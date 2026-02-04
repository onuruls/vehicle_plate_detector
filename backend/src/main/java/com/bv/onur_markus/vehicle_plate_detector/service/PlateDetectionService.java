package com.bv.onur_markus.vehicle_plate_detector.service;

import com.bv.onur_markus.vehicle_plate_detector.utils.*;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class PlateDetectionService {

    static {
        OpenCV.loadLocally();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PlateDetectionService.class);

    @Value("${app.debug.enabled:false}")
    private boolean debugEnabled;

    @Value("${app.debug.dir:${java.io.tmpdir}/vehicle-plate-debug}")
    private String debugDir;

    private final ImagePreprocessor imagePreprocessor;
    private final PlateDetector plateDetector;
    private final CharacterSegmenter characterSegmenter;
    private final CharacterRecognizer characterRecognizer;

    public PlateDetectionService(TemplateLoader templateLoader) {
        this.imagePreprocessor = new ImagePreprocessor();
        this.plateDetector = new PlateDetector();
        this.characterSegmenter = new CharacterSegmenter();
        this.characterRecognizer = new CharacterRecognizer(templateLoader.getTemplates());
    }

    public String detectPlate(MultipartFile file) {
        try {
            Path tempFile = Files.createTempFile("plate-", "-" + file.getOriginalFilename());
            file.transferTo(tempFile.toFile());
            try {
                return detectPlateFromFile(tempFile.toFile());
            } finally {
                Files.deleteIfExists(tempFile);
            }
        } catch (IOException e) {
            LOGGER.error("Error processing file", e);
            return "Error processing file";
        }
    }

    public String detectPlateFromBase64(String base64Image) {
        try {
            String[] parts = base64Image.split(",");
            if (parts.length != 2) {
                return "Invalid base64 image format";
            }
            String imageString = parts[1];
            byte[] decodedBytes = Base64.getDecoder().decode(imageString);

            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(decodedBytes));
            Path tempFile = Files.createTempFile("plate-base64-", ".png");
            ImageIO.write(bufferedImage, "png", tempFile.toFile());

            try {
                return detectPlateFromFile(tempFile.toFile());
            } finally {
                Files.deleteIfExists(tempFile);
            }
        } catch (IOException e) {
            LOGGER.error("Error decoding image", e);
            return "Error decoding image";
        }
    }

    private String detectPlateFromFile(File file) {
        Mat src = Imgcodecs.imread(file.getAbsolutePath());

        int[] thresholds = { 15, 10, 5, 0 };
        PlateDetectionResult bestResult = null;
        List<Rect> allDetectedPlates = new ArrayList<>();
        List<Mat> bestSegmentedChars = new ArrayList<>();

        for (int threshold : thresholds) {
            Mat processedImage = imagePreprocessor.preprocessImage(src, threshold);
            List<Rect> possiblePlates = plateDetector.detectPossiblePlates(processedImage);
            allDetectedPlates.addAll(possiblePlates);

            for (Rect plateRect : possiblePlates) {
                Mat normalizedPlate = Utils.normalizePlate(new Mat(src, plateRect));
                List<Mat> segmentedChars = characterSegmenter.segmentCharacters(normalizedPlate);
                int charCount = segmentedChars.size();

                if (bestResult == null || charCount > bestResult.charCount()) {
                    bestResult = new PlateDetectionResult(plateRect, "", charCount, threshold, processedImage.clone());
                    bestSegmentedChars = new ArrayList<>(segmentedChars);
                }
            }
        }

        if (bestResult != null) {
            if (debugEnabled) {
                saveDebugImages(src, bestResult, allDetectedPlates, bestSegmentedChars);
            }
            return characterRecognizer.extractPlateText(bestSegmentedChars);
        }

        return "Not found";
    }

    private void saveDebugImages(Mat src, PlateDetectionResult bestResult,
            List<Rect> allDetectedPlates, List<Mat> segmentedChars) {
        try {
            Path debugPath = Path.of(debugDir);
            Files.createDirectories(debugPath);

            // Save best threshold image
            Imgcodecs.imwrite(debugPath.resolve("best_thresh_image.bmp").toString(), bestResult.processedImage);

            // Save with rectangles
            Mat bestThreshWithRect = bestResult.processedImage.clone();
            Imgproc.rectangle(bestThreshWithRect, bestResult.rect(), new Scalar(0, 255, 0), 5);
            Imgcodecs.imwrite(debugPath.resolve("best_thresh_image_with_rectangles.bmp").toString(),
                    bestThreshWithRect);

            // Save all detected plates
            Mat originalWithAllPlates = src.clone();
            for (Rect plate : allDetectedPlates) {
                Imgproc.rectangle(originalWithAllPlates, plate, new Scalar(0, 0, 255), 5);
            }
            Imgcodecs.imwrite(debugPath.resolve("all_detected_plates.bmp").toString(), originalWithAllPlates);

            // Save best plate
            Mat originalWithBest = src.clone();
            Imgproc.rectangle(originalWithBest, bestResult.rect(), new Scalar(0, 255, 0), 5);
            Imgcodecs.imwrite(debugPath.resolve("best_detected_plate.bmp").toString(), originalWithBest);

            // Save segmented chars
            Path charsDir = debugPath.resolve("segmented_chars");
            Files.createDirectories(charsDir);
            for (int i = 0; i < segmentedChars.size(); i++) {
                Imgcodecs.imwrite(charsDir.resolve("char_" + i + ".png").toString(), segmentedChars.get(i));
            }

            LOGGER.debug("Debug images saved to {}", debugPath);
        } catch (IOException e) {
            LOGGER.warn("Failed to save debug images", e);
        }
    }

    private record PlateDetectionResult(Rect rect, String filteredOcrResult, int charCount, int threshold,
            Mat processedImage) {
    }
}
