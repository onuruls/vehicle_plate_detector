package com.bv.onur_markus.vehicle_plate_detector.utils;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class PlateDetector {

    /**
     * Detects possible plate regions from a preprocessed image by finding and analyzing contours.
     * @param preprocessedImage The image that has already been processed to enhance features relevant for plate detection.
     * @return A list of bounding rectangles that potentially correspond to license plates.
     */
    public List<Rect> detectPossiblePlates(Mat preprocessedImage) {
        List<Rect> possiblePlates = new ArrayList<>();
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        // Find contours in the preprocessed image. This method identifies all continuous points having the same color or intensity.
        Imgproc.findContours(preprocessedImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Analyze each contour to determine if it could be a license plate.
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour); // Calculates the up-right bounding rectangle of a point set.
            double aspectRatio = (double) rect.width / rect.height;
            double area = Imgproc.contourArea(contour); // Calculates the area of the contour.

            // Check if the contour matches typical license plate criteria.
            if (aspectRatio >= 2 && aspectRatio <= 7 && area > 100) { // Aspect ratio and area conditions for typical license plates.
                possiblePlates.add(rect); // Add the bounding rectangle of the contour to the list of possible plates.
            }
        }

        return possiblePlates; // Return the list of potential license plate areas.
    }
}
