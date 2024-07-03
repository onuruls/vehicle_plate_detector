package com.bv.onur_markus.vehicle_plate_detector.service;

import com.bv.onur_markus.vehicle_plate_detector.utils.*;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class PlateDetectionService {

    static {
        OpenCV.loadLocally();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PlateDetectionService.class);
    private final ImagePreprocessor imagePreprocessor;
    private final PlateDetector plateDetector;
    private final CharacterSegmenter characterSegmenter;
    private final CharacterRecognizer characterRecognizer;

    public PlateDetectionService() {
        TemplateLoader templateLoader = new TemplateLoader();
        this.imagePreprocessor = new ImagePreprocessor();
        this.plateDetector = new PlateDetector();
        this.characterSegmenter = new CharacterSegmenter();
        this.characterRecognizer = new CharacterRecognizer(templateLoader.getTemplates());
    }

    public String detectPlate(MultipartFile file) {
        try {
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
            file.transferTo(convFile);
            return detectPlateFromFile(convFile);
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
            File imageFile = new File("received_image.png");
            ImageIO.write(bufferedImage, "png", imageFile);

            return detectPlateFromFile(imageFile);
        } catch (IOException e) {
            LOGGER.error("Error decoding image", e);
            return "Error decoding image";
        }
    }

    private String detectPlateFromFile(File file) {
        // Clear the segmented_chars directory
        Utils.clearSegmentedCharsDirectory();

        // Read the image file
        Mat src = Imgcodecs.imread(file.getAbsolutePath());

        // Test multiple thresholds
        int[] thresholds = {15, 10, 5, 0};
        PlateDetectionResult bestResult = null;

        for (int threshold : thresholds) {
            Mat processedImage = imagePreprocessor.preprocessImage(src, threshold);
            List<Rect> possiblePlates = plateDetector.detectPossiblePlates(processedImage);
            for (Rect plateRect : possiblePlates) {
                Mat normalizedPlate = Utils.normalizePlate(new Mat(src, plateRect));
                List<Mat> segmentedChars = characterSegmenter.segmentCharacters(normalizedPlate);
                int charCount = segmentedChars.size();

                if (bestResult == null || charCount > bestResult.charCount()) {
                    bestResult = new PlateDetectionResult(plateRect, "", charCount, threshold, processedImage.clone());
                    Utils.saveSegmentedChars(segmentedChars);
                }
            }
        }

        if (bestResult != null) {
            Imgcodecs.imwrite("src/main/resources/best_thresh_image.bmp", bestResult.processedImage);
            Imgcodecs.imwrite("src/main/resources/best_plate_image.bmp", new Mat(src, bestResult.rect()));

            // Extract plate text
            return characterRecognizer.extractPlateText();
        }

        return "Not found";
    }

    private record PlateDetectionResult(Rect rect, String filteredOcrResult, int charCount, int threshold, Mat processedImage) {
    }
}
