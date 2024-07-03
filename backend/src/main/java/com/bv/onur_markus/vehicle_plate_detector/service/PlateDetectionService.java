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
        // Load the OpenCV library at the start of the application.
        OpenCV.loadLocally();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PlateDetectionService.class);
    private final ImagePreprocessor imagePreprocessor;
    private final PlateDetector plateDetector;
    private final CharacterSegmenter characterSegmenter;
    private final CharacterRecognizer characterRecognizer;

    /**
     * Constructor initializes components required for plate detection.
     */
    public PlateDetectionService() {
        TemplateLoader templateLoader = new TemplateLoader();
        this.imagePreprocessor = new ImagePreprocessor();
        this.plateDetector = new PlateDetector();
        this.characterSegmenter = new CharacterSegmenter();
        this.characterRecognizer = new CharacterRecognizer(templateLoader.getTemplates());
    }

    /**
     * Processes a multipart file to detect a license plate and extract its text.
     */
    public String detectPlate(MultipartFile file) {
        try {
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
            file.transferTo(convFile); // Save the uploaded file to the temporary directory
            return detectPlateFromFile(convFile); // Process the saved file to detect the plate
        } catch (IOException e) {
            LOGGER.error("Error processing file", e);
            return "Error processing file";
        }
    }

    /**
     * Processes a base64 image string to detect a license plate and extract its text.
     */
    public String detectPlateFromBase64(String base64Image) {
        try {
            String[] parts = base64Image.split(",");
            if (parts.length != 2) {
                return "Invalid base64 image format";
            }
            byte[] decodedBytes = Base64.getDecoder().decode(parts[1]);
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(decodedBytes));
            File imageFile = new File("received_image.png");
            ImageIO.write(bufferedImage, "png", imageFile);

            return detectPlateFromFile(imageFile); // Process the decoded image file to detect the plate
        } catch (IOException e) {
            LOGGER.error("Error decoding image", e);
            return "Error decoding image";
        }
    }

    /**
     * Detects a license plate from a file image and attempts to recognize its characters.
     */
    private String detectPlateFromFile(File file) {
        Utils.clearSegmentedCharsDirectory(); // Clear any previously segmented character images

        // Array of threshold values used to optimize image processing during plate detection.
        // Each threshold is tested to determine which provides the best clarity for character segmentation.
        int[] thresholds = {15, 10, 5, 0};

        Mat src = Imgcodecs.imread(file.getAbsolutePath()); // Read the image from file
        PlateDetectionResult bestResult = null;

        // Test different threshold levels to find the most clear result
        for (int threshold : thresholds) {
            Mat processedImage = imagePreprocessor.preprocessImage(src, threshold);
            List<Rect> possiblePlates = plateDetector.detectPossiblePlates(processedImage);
            for (Rect plateRect : possiblePlates) {
                Mat normalizedPlate = Utils.normalizePlate(new Mat(src, plateRect));
                List<Mat> segmentedChars = characterSegmenter.segmentCharacters(normalizedPlate);
                int charCount = segmentedChars.size();

                // Choose the result with the highest number of recognized characters
                if (bestResult == null || charCount > bestResult.charCount()) {
                    bestResult = new PlateDetectionResult(plateRect, "", charCount, threshold, processedImage.clone());
                    Utils.saveSegmentedChars(segmentedChars);
                }
            }
        }

        if (bestResult != null) {
            // Save images for debugging or review
            Imgcodecs.imwrite("src/main/resources/best_thresh_image.png", bestResult.processedImage());
            Imgcodecs.imwrite("src/main/resources/best_plate_image.png", new Mat(src, bestResult.rect()));

            return characterRecognizer.extractPlateText(); // Extract the text from the detected plate
        }

        return "Not found"; // Return not found if no valid plate was detected
    }

    /**
     * A simple data structure to hold the results of plate detection.
     */
    private record PlateDetectionResult(Rect rect, String filteredOcrResult, int charCount, int threshold, Mat processedImage) {
    }
}
