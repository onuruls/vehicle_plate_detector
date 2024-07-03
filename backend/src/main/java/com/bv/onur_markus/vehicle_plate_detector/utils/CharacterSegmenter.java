package com.bv.onur_markus.vehicle_plate_detector.utils;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CharacterSegmenter {

    /**
     * Processes the input plate to segment out potential characters.
     * Converts the plate to grayscale, identifies contours, and extracts each character.
     */
    public List<Mat> segmentCharacters(Mat plate) {
        Mat grayPlate = convertToGrayScale(plate);
        List<MatOfPoint> contours = findContours(grayPlate);
        List<Rect> boundingRects = getBoundingRects(contours);
        return sortCharImages(boundingRects, grayPlate);
    }

    /**
     * Converts the input image to grayscale and applies a binary threshold for clearer contour detection.
     */
    private Mat convertToGrayScale(Mat plate) {
        Mat grayPlate = new Mat();
        Imgproc.cvtColor(plate, grayPlate, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(grayPlate, grayPlate, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        return grayPlate;
    }

    /**
     * Detects contours in the grayscale image which represent possible characters.
     */
    private List<MatOfPoint> findContours(Mat grayPlate) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(grayPlate, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }

    /**
     * Calculates bounding rectangles for each contour to potentially identify characters.
     */
    private List<Rect> getBoundingRects(List<MatOfPoint> contours) {
        List<Rect> boundingRects = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Rect charRect = Imgproc.boundingRect(contour);
            if (Utils.isCharSizeValid(charRect) && Utils.isAspectRatioValid((double) charRect.width / charRect.height)) {
                boundingRects.add(charRect);
            }
        }
        boundingRects.sort(Comparator.comparingInt(rect -> rect.x));
        return boundingRects;
    }

    /**
     * Extracts and sorts images of each character based on their bounding rectangles.
     */
    private List<Mat> sortCharImages(List<Rect> boundingRects, Mat grayPlate) {
        List<Mat> sortedCharImages = new ArrayList<>();
        for (Rect rect : boundingRects) {
            sortedCharImages.add(new Mat(grayPlate, rect));
        }
        return sortedCharImages;
    }
}
