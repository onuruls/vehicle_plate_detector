package com.bv.onur_markus.vehicle_plate_detector.utils;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.List;

public class Utils {

    public static void clearSegmentedCharsDirectory() {
        File directory = new File("src/main/resources/segmented_chars");
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
    }

    public static Mat normalizePlate(Mat plate) {
        double aspectRatio = (double) plate.width() / plate.height();
        int targetHeight = 100;
        int targetWidth = (int) (targetHeight * aspectRatio);

        Mat normalizedPlate = new Mat();
        Imgproc.resize(plate, normalizedPlate, new Size(targetWidth, targetHeight));
        return normalizedPlate;
    }

    public static void saveSegmentedChars(List<Mat> segmentedChars) {
        String outputDir = "src/main/resources/segmented_chars";
        new File(outputDir).mkdirs();

        int index = 0;
        for (Mat charImg : segmentedChars) {
            Imgcodecs.imwrite(outputDir + "/char_" + index + ".png", charImg);
            index++;
        }
    }

    public static Mat createSpaceMat() {
        return Mat.zeros(new Size(48, 48), CvType.CV_8UC1);
    }

    public static boolean isCharSizeValid(Rect rect) {
        int minWidth = 10;
        int maxWidth = 80;
        int minHeight = 60;
        int maxHeight = 100;
        return rect.width >= minWidth && rect.width <= maxWidth && rect.height >= minHeight && rect.height <= maxHeight;
    }

    public static boolean isAspectRatioValid(double aspectRatio) {
        double minAspectRatio = 0.2;
        double maxAspectRatio = 1.0;
        return aspectRatio >= minAspectRatio && aspectRatio <= maxAspectRatio;
    }
}
