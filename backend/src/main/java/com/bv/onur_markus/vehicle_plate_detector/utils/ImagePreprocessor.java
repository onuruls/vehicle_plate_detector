package com.bv.onur_markus.vehicle_plate_detector.utils;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImagePreprocessor {

    public Mat preprocessImage(Mat src, int threshold) {
        Mat gray = new Mat();
        Mat blurred = new Mat();
        Mat equalized = new Mat();
        Mat blackhat = new Mat();
        Mat thresh = new Mat();

        // Convert to grayscale
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        // Apply Gaussian blur
        Imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);

        // Equalize histogram to improve contrast
        Imgproc.equalizeHist(blurred, equalized);
        Imgcodecs.imwrite("src/main/resources/gray.bmp", equalized);

        // Apply blackhat morphology to highlight dark regions on light background
        Mat rectKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(50, 25));
        Imgproc.morphologyEx(equalized, blackhat, Imgproc.MORPH_BLACKHAT, rectKernel);
        Imgcodecs.imwrite("src/main/resources/blackhat.bmp", blackhat);

        // Apply adaptive thresholding
        Imgproc.adaptiveThreshold(blackhat, thresh, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 25, threshold);
        Imgcodecs.imwrite("src/main/resources/thresh.bmp", thresh);

        return thresh;
    }
}
