package com.bv.onur_markus.vehicle_plate_detector.utils;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.util.List;
import java.util.Map;

/**
 * Recognizes characters from segmented plate images using template matching.
 */
public class CharacterRecognizer {
    private final Map<Character, List<Mat>> templates;

    public CharacterRecognizer(Map<Character, List<Mat>> templates) {
        this.templates = templates;
    }

    /**
     * Extract plate text from segmented character images.
     * 
     * @param segmentedChars List of character image matrices
     * @return The recognized plate text
     */
    public String extractPlateText(List<Mat> segmentedChars) {
        if (segmentedChars == null || !isValidPlateLength(segmentedChars.size())) {
            return "Not found";
        }

        StringBuilder plateText = new StringBuilder();
        for (Mat charImg : segmentedChars) {
            if (isDarkSpaceMat(charImg)) {
                plateText.append(" ");
            } else {
                char bestMatch = findBestTemplateMatch(charImg);
                plateText.append(bestMatch);
            }
        }
        return plateText.toString();
    }

    private boolean isDarkSpaceMat(Mat charImg) {
        return Core.mean(charImg).val[0] == 0;
    }

    private char findBestTemplateMatch(Mat charImg) {
        char bestMatch = '?';
        double bestAverageScore = Double.MAX_VALUE;

        for (Map.Entry<Character, List<Mat>> entry : templates.entrySet()) {
            char templateChar = entry.getKey();
            List<Mat> templateVariants = entry.getValue();
            double totalScore = 0.0;

            for (Mat template : templateVariants) {
                Mat result = new Mat();
                Imgproc.matchTemplate(charImg, template, result, Imgproc.TM_SQDIFF_NORMED);
                Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
                totalScore += mmr.minVal;
            }

            double averageScore = totalScore / templateVariants.size();

            if (averageScore < bestAverageScore) {
                bestAverageScore = averageScore;
                bestMatch = templateChar;
            }
        }

        return bestMatch;
    }

    private boolean isValidPlateLength(int length) {
        return length >= 6 && length <= 10;
    }
}
