package com.bv.onur_markus.vehicle_plate_detector.utils;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class PlateDetector {

    public List<Rect> detectPossiblePlates(Mat preprocessedImage) {
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
}
