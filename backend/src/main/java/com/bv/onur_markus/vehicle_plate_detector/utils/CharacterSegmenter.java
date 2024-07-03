package com.bv.onur_markus.vehicle_plate_detector.utils;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CharacterSegmenter {

    public List<Mat> segmentCharacters(Mat plate) {
        Mat grayPlate = convertToGrayScale(plate);
        List<MatOfPoint> contours = findContours(grayPlate);

        List<Rect> boundingRects = getBoundingRects(contours);
        List<Mat> sortedCharImages = sortCharImages(boundingRects, grayPlate);

        double[] meanAndStdDev = calculateMeanAndStdDev(sortedCharImages);
        double mean = meanAndStdDev[0];
        double stdDev = meanAndStdDev[1];

        List<Mat> filteredChars = filterAndEnhanceCharacters(sortedCharImages, mean, stdDev);
        List<Integer> distances = calculateDistances(boundingRects);

        return insertSpaces(filteredChars, distances, stdDev);
    }

    private Mat convertToGrayScale(Mat plate) {
        Mat grayPlate = new Mat();
        Imgproc.cvtColor(plate, grayPlate, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(grayPlate, grayPlate, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        return grayPlate;
    }

    private List<MatOfPoint> findContours(Mat grayPlate) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(grayPlate, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }

    private List<Rect> getBoundingRects(List<MatOfPoint> contours) {
        List<Rect> boundingRects = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            Rect charRect = Imgproc.boundingRect(contour);
            double aspectRatio = (double) charRect.width / charRect.height;
            if (Utils.isCharSizeValid(charRect) && Utils.isAspectRatioValid(aspectRatio)) {
                boundingRects.add(charRect);
            }
        }
        boundingRects.sort(Comparator.comparingInt(rect -> rect.x));
        return boundingRects;
    }

    private List<Mat> sortCharImages(List<Rect> boundingRects, Mat grayPlate) {
        List<Mat> sortedCharImages = new ArrayList<>();
        for (Rect rect : boundingRects) {
            sortedCharImages.add(new Mat(grayPlate, rect));
        }
        return sortedCharImages;
    }

    private List<Mat> filterAndEnhanceCharacters(List<Mat> sortedCharImages, double mean, double stdDev) {
        List<Mat> filteredChars = new ArrayList<>();
        for (Mat charImg : sortedCharImages) {
            enhanceCharacter(charImg);
            resizeCharacter(charImg);
            filteredChars.add(charImg);
        }
        return filteredChars;
    }

    private void enhanceCharacter(Mat charImg) {
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.dilate(charImg, charImg, kernel);
        Imgproc.erode(charImg, charImg, kernel);
    }

    private void resizeCharacter(Mat charImg) {
        double aspectRatio = (double) charImg.width() / charImg.height();
        int targetHeight = 48;
        int targetWidth = (int) (targetHeight * aspectRatio);
        Imgproc.resize(charImg, charImg, new Size(targetWidth, targetHeight));
    }

    private List<Integer> calculateDistances(List<Rect> boundingRects) {
        List<Integer> distances = new ArrayList<>();
        for (int i = 1; i < boundingRects.size(); i++) {
            Rect prevRect = boundingRects.get(i - 1);
            Rect currRect = boundingRects.get(i);
            int distance = currRect.x - (prevRect.x + prevRect.width);
            distances.add(distance);
        }
        return distances;
    }

    private List<Mat> insertSpaces(List<Mat> filteredChars, List<Integer> distances, double stdDev) {
        List<Mat> charsWithSpaces = new ArrayList<>();
        for (int i = 0; i < filteredChars.size(); i++) {
            charsWithSpaces.add(filteredChars.get(i));
            if (i < distances.size() && distances.get(i) > stdDev * 3) {
                charsWithSpaces.add(Utils.createSpaceMat());
            }
        }
        return charsWithSpaces;
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
}
