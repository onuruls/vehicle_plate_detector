package com.bv.onur_markus.vehicle_plate_detector.utils;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


public class CharacterRecognizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CharacterRecognizer.class);
    private final Map<Character, List<Mat>> templates;

    public CharacterRecognizer(Map<Character, List<Mat>> templates) {
        this.templates = templates;
    }

    public String extractPlateText() {
        StringBuilder plateText = new StringBuilder();

        File segmentedDir = new File("src/main/resources/segmented_chars");
        File[] files = segmentedDir.listFiles((dir, name) -> name.startsWith("char_") && name.endsWith(".png"));

        if (files != null && isValidPlateLength(files.length)) {
            Arrays.sort(files, Comparator.comparingInt(f -> Integer.parseInt(f.getName().replace("char_", "").replace(".png", ""))));
            for (File file : files) {
                try {
                    Mat charImg = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
                    if (isDarkSpaceMat(charImg)) {
                        plateText.append(" ");
                    } else {
                        char bestMatch = findBestTemplateMatch(charImg);
                        plateText.append(bestMatch);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error reading segmented char image", e);
                }
            }
            return plateText.toString();
        }
        return "Not found";
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
