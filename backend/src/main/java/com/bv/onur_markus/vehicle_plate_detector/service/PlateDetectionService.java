package com.bv.onur_markus.vehicle_plate_detector.service;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
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
    private final Map<Character, Mat> templates = new HashMap<>();

    public PlateDetectionService() {
        loadTemplates();
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
        clearSegmentedCharsDirectory();

        // Read the image file
        Mat src = Imgcodecs.imread(file.getAbsolutePath());

        // Test multiple thresholds
        int[] thresholds = {15, 10, 5, 0};
        PlateDetectionResult bestResult = null;

        for (int threshold : thresholds) {
            Mat processedImage = preprocessImage(src, threshold);
            List<Rect> possiblePlates = detectPossiblePlates(processedImage);
            for (Rect plateRect : possiblePlates) {
                Mat normalizedPlate = normalizePlate(new Mat(src, plateRect));
                List<Mat> segmentedChars = segmentCharacters(normalizedPlate);
                int charCount = segmentedChars.size();

                if (bestResult == null || charCount > bestResult.charCount()) {
                    bestResult = new PlateDetectionResult(plateRect, "", charCount, threshold, processedImage.clone());
                    saveSegmentedChars(segmentedChars);
                }
            }
        }

        if (bestResult != null) {
            Imgcodecs.imwrite("src/main/resources/best_thresh_image.png", bestResult.processedImage());
            Imgcodecs.imwrite("src/main/resources/best_plate_image.png", new Mat(src, bestResult.rect()));

            // Extract plate text
            String plateText = extractPlateText(bestResult);
            return "Plate found: " + plateText + " with " + bestResult.charCount() + " characters.";
        }

        return "Not found";
    }

    private String extractPlateText(PlateDetectionResult bestResult) {
        StringBuilder plateText = new StringBuilder();

        File segmentedDir = new File("src/main/resources/segmented_chars");
        File[] files = segmentedDir.listFiles((dir, name) -> name.startsWith("char_") && name.endsWith(".png"));

        if (files != null) {
            Arrays.sort(files, Comparator.comparingInt(f -> Integer.parseInt(f.getName().replace("char_", "").replace(".png", ""))));
            for (File file : files) {
                try {
                    Mat charImg = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
                    char bestMatch = findBestTemplateMatch(charImg);
                    plateText.append(bestMatch);
                } catch (Exception e) {
                    LOGGER.error("Error reading segmented char image", e);
                }
            }
        }

        return plateText.toString();
    }

    private void clearSegmentedCharsDirectory() {
        File directory = new File("src/main/resources/segmented_chars");
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
    }

    private Mat preprocessImage(Mat src, int threshold) {
        Mat gray = new Mat();

        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
        Imgproc.equalizeHist(gray, gray);

        Mat blackhat = new Mat();
        Mat rectKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(13, 5));
        Imgproc.morphologyEx(gray, blackhat, Imgproc.MORPH_BLACKHAT, rectKernel);

        Mat thresh = new Mat();
        Imgproc.adaptiveThreshold(blackhat, thresh, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 25, threshold);

        return thresh;
    }

    private List<Rect> detectPossiblePlates(Mat preprocessedImage) {
        List<Rect> possiblePlates = new ArrayList<>();
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(preprocessedImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            double aspectRatio = (double) rect.width / rect.height;
            double area = Imgproc.contourArea(contour);

            if (aspectRatio >= 2 && aspectRatio <= 7 && area > 100) {
                possiblePlates.add(rect);
            }
        }

        return possiblePlates;
    }

    private Mat normalizePlate(Mat plate) {
        double aspectRatio = (double) plate.width() / plate.height();
        int targetHeight = 100;
        int targetWidth = (int) (targetHeight * aspectRatio);

        Mat normalizedPlate = new Mat();
        Imgproc.resize(plate, normalizedPlate, new Size(targetWidth, targetHeight));
        return normalizedPlate;
    }

    private double calculateAverageIntensity(Mat image) {
        if (image.channels() > 1) {
            Mat gray = new Mat();
            Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
            return Core.mean(gray).val[0];
        } else {
            return Core.mean(image).val[0];
        }
    }

    private double[] calculateMeanAndStdDev(List<Mat> images) {
        List<Double> intensities = new ArrayList<>();
        for (Mat img : images) {
            intensities.add(calculateAverageIntensity(img));
        }

        double mean = intensities.stream().mapToDouble(val -> val).average().orElse(0.0);
        double stdDev = Math.sqrt(intensities.stream().mapToDouble(val -> Math.pow(val - mean, 2)).average().orElse(0.0));

        return new double[]{mean, stdDev};
    }

    private List<Mat> segmentCharacters(Mat plate) {
        Mat grayPlate = new Mat();
        Imgproc.cvtColor(plate, grayPlate, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(grayPlate, grayPlate, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(grayPlate, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        List<Rect> boundingRects = new ArrayList<>();
        List<Mat> charImages = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Rect charRect = Imgproc.boundingRect(contour);
            double aspectRatio = (double) charRect.width / charRect.height;
            if (isCharSizeValid(charRect) && isAspectRatioValid(aspectRatio)) {
                boundingRects.add(charRect);
                charImages.add(new Mat(grayPlate, charRect));
            }
        }

        List<Mat> sortedCharImages = new ArrayList<>();
        boundingRects.stream()
                .sorted(Comparator.comparingInt(rect -> rect.x))
                .forEachOrdered(rect -> {
                    for (int i = 0; i < boundingRects.size(); i++) {
                        if (boundingRects.get(i).equals(rect)) {
                            sortedCharImages.add(charImages.get(i));
                            break;
                        }
                    }
                });

        double[] meanAndStdDev = calculateMeanAndStdDev(sortedCharImages);
        double mean = meanAndStdDev[0];
        double stdDev = meanAndStdDev[1];

        List<Mat> filteredChars = new ArrayList<>();
        List<Integer> distances = new ArrayList<>();
        for (int i = 0; i < sortedCharImages.size(); i++) {
            Mat charImg = sortedCharImages.get(i);
            double averageIntensity = calculateAverageIntensity(charImg);
            if (Math.abs(averageIntensity - mean) <= 1.5 * stdDev) {
                // Enhance the character by applying dilation and erosion
                Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
                Imgproc.dilate(charImg, charImg, kernel);
                Imgproc.erode(charImg, charImg, kernel);

                // Resize the character to its natural proportion
                double aspectRatio = (double) charImg.width() / charImg.height();
                int targetHeight = 48;
                int targetWidth = (int) (targetHeight * aspectRatio);
                Imgproc.resize(charImg, charImg, new Size(targetWidth, targetHeight));

                filteredChars.add(charImg);

                // Calculate distances between characters
                if (i > 0) {
                    Rect prevRect = boundingRects.get(i - 1);
                    Rect currRect = boundingRects.get(i);
                    int distance = currRect.x - (prevRect.x + prevRect.width);
                    distances.add(distance);
                }
            }
        }

        // Insert spaces based on distances between contours
        return insertSpaces(filteredChars, distances, stdDev);
    }


    private List<Mat> insertSpaces(List<Mat> filteredChars, List<Integer> distances, double stdDev) {
        List<Mat> charsWithSpaces = new ArrayList<>();

        for (int i = 0; i < filteredChars.size(); i++) {
            charsWithSpaces.add(filteredChars.get(i));
            if (i < distances.size() && distances.get(i) > stdDev) {
                charsWithSpaces.add(createSpaceMat());
            }
        }
        return charsWithSpaces;
    }

    private Mat createSpaceMat() {
        return Mat.zeros(new Size(48, 48), CvType.CV_8UC1);
    }

    private boolean isCharSizeValid(Rect rect) {
        int minWidth = 10;
        int maxWidth = 80;
        int minHeight = 60;
        int maxHeight = 100;
        return rect.width >= minWidth && rect.width <= maxWidth && rect.height >= minHeight && rect.height <= maxHeight;
    }

    private boolean isAspectRatioValid(double aspectRatio) {
        double minAspectRatio = 0.2;
        double maxAspectRatio = 1.0;
        return aspectRatio >= minAspectRatio && aspectRatio <= maxAspectRatio;
    }

    private void saveSegmentedChars(List<Mat> segmentedChars) {
        String outputDir = "src/main/resources/segmented_chars";
        new File(outputDir).mkdirs();

        int index = 0;
        for (Mat charImg : segmentedChars) {
            Imgcodecs.imwrite(outputDir + "/char_" + index + ".png", charImg);
            index++;
        }
    }

    private void loadTemplates() {
        String templateDir = "src/main/resources/templates";
        File dir = new File(templateDir);

        if (dir.exists() && dir.isDirectory()) {
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                if (file.isFile() && file.getName().endsWith(".png")) {
                    String name = file.getName().replace(".png", "");
                    char templateChar = name.charAt(0);
                    Mat template = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
                    templates.put(templateChar, template);
                }
            }
        } else {
            throw new RuntimeException("Template directory not found: " + templateDir);
        }
    }

    private List<Mat> assignCharactersToTemplates(List<Mat> filteredChars) {
        List<Mat> assignedChars = new ArrayList<>();

        for (Mat charImg : filteredChars) {
            char bestMatch = findBestTemplateMatch(charImg);
            Mat annotatedCharImg = annotateCharacter(charImg, bestMatch);
            assignedChars.add(annotatedCharImg);
        }

        return assignedChars;
    }

    private char findBestTemplateMatch(Mat charImg) {
        char bestMatch = '?';
        double bestMatchScore = Double.MAX_VALUE;

        for (Map.Entry<Character, Mat> entry : templates.entrySet()) {
            Mat result = new Mat();
            Imgproc.matchTemplate(charImg, entry.getValue(), result, Imgproc.TM_SQDIFF_NORMED);
            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

            if (mmr.minVal < bestMatchScore) {
                bestMatchScore = mmr.minVal;
                bestMatch = entry.getKey();
            }
        }

        return bestMatch;
    }

    private Mat annotateCharacter(Mat charImg, char bestMatch) {
        Mat annotatedImg = charImg.clone();
        Imgproc.putText(annotatedImg, String.valueOf(bestMatch), new Point(5, 20), Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(255, 255, 255), 2);
        return annotatedImg;
    }

    private record PlateDetectionResult(Rect rect, String filteredOcrResult, int charCount, int threshold, Mat processedImage) {
    }
}
