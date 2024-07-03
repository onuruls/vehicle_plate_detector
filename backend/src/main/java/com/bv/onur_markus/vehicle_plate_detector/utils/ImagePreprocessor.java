package com.bv.onur_markus.vehicle_plate_detector.utils;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ImagePreprocessor {

    /**
     * Applies a series of image preprocessing techniques to enhance features of interest in a source image.
     * @param src The source image to preprocess.
     * @param threshold The threshold value used for adaptive thresholding.
     * @return A Mat object that contains the preprocessed image data.
     */
    public Mat preprocessImage(Mat src, int threshold) {
        Mat gray = new Mat();
        Mat blurred = new Mat();
        Mat equalized = new Mat();
        Mat blackhat = new Mat();
        Mat thresh = new Mat();

        // Convert the source image to grayscale to reduce complexity.
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        // Apply Gaussian blur to reduce noise which can enhance edge detection later.
        Imgproc.GaussianBlur(gray, blurred, new Size(5, 5), 0);

        // Equalize the histogram of the blurred image to improve the contrast of the image.
        Imgproc.equalizeHist(blurred, equalized);
        Imgcodecs.imwrite("src/main/resources/gray.bmp", equalized);

        // Use a blackhat morphological operation to highlight dark regions on a lighter background.
        Mat rectKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(50, 25));
        Imgproc.morphologyEx(equalized, blackhat, Imgproc.MORPH_BLACKHAT, rectKernel);
        Imgcodecs.imwrite("src/main/resources/blackhat.bmp", blackhat);

        // Apply adaptive thresholding to create a binary image where the foreground is separated from the background.
        Imgproc.adaptiveThreshold(blackhat, thresh, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 25, threshold);
        Imgcodecs.imwrite("src/main/resources/thresh.bmp", thresh);

        return thresh;  // Return the thresholded image, which is ready for further processing or analysis.
    }
}
