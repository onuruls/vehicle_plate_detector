package com.bv.onur_markus.vehicle_plate_detector.utils;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.List;

public class Utils {

    /**
     * Clears all files in the segmented characters directory.
     */
    public static void clearSegmentedCharsDirectory() {
        File directory = new File("src/main/resources/segmented_chars");
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    file.delete(); // Delete each file within the directory
                }
            }
        }
    }

    /**
     * Normalizes the size of the license plate image to a consistent dimension.
     * @param plate The source Mat image of the plate.
     * @return A resized Mat image of the plate.
     */
    public static Mat normalizePlate(Mat plate) {
        double aspectRatio = (double) plate.width() / plate.height();
        int targetHeight = 100;
        int targetWidth = (int) (targetHeight * aspectRatio);

        Mat normalizedPlate = new Mat();
        Imgproc.resize(plate, normalizedPlate, new Size(targetWidth, targetHeight));
        return normalizedPlate;
    }

    /**
     * Saves segmented character images to a directory.
     * @param segmentedChars A list of Mat images representing segmented characters.
     */
    public static void saveSegmentedChars(List<Mat> segmentedChars) {
        String outputDir = "src/main/resources/segmented_chars";
        new File(outputDir).mkdirs(); // Ensure the directory exists

        int index = 0;
        for (Mat charImg : segmentedChars) {
            Imgcodecs.imwrite(outputDir + "/char_" + index + ".png", charImg); // Save each character image as a PNG file
            index++;
        }
    }

    /**
     * Creates a blank Mat image used to represent spaces between characters.
     * @return A Mat image filled with zeros (black).
     */
    public static Mat createSpaceMat() {
        return Mat.zeros(new Size(48, 48), CvType.CV_8UC1);
    }

    /**
     * Validates if the size of the detected character is within acceptable limits.
     * @param rect The bounding rectangle of the detected character.
     * @return True if the size is valid, false otherwise.
     */
    public static boolean isCharSizeValid(Rect rect) {
        int minWidth = 10;
        int maxWidth = 80;
        int minHeight = 60;
        int maxHeight = 100;
        return rect.width >= minWidth && rect.width <= maxWidth && rect.height >= minHeight && rect.height <= maxHeight;
    }

    /**
     * Validates the aspect ratio of the detected character to determine if it is likely to be a valid character.
     * @param aspectRatio The aspect ratio of the character.
     * @return True if the aspect ratio is within the valid range, false otherwise.
     */
    public static boolean isAspectRatioValid(double aspectRatio) {
        double minAspectRatio = 0.2; // Minimum aspect ratio to accept as a valid character
        double maxAspectRatio = 1.0; // Maximum aspect ratio to accept as a valid character
        return aspectRatio >= minAspectRatio && aspectRatio <= maxAspectRatio;
    }
}
