package com.bv.onur_markus.vehicle_plate_detector.utils;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.regex.Pattern;


public class CharacterRecognizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CharacterRecognizer.class);
    private final Map<Character, Mat> templates;

    public CharacterRecognizer(Map<Character, Mat> templates) {
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
        double bestMatchScore = Double.MAX_VALUE;

        for (Map.Entry<Character, Mat> entry : templates.entrySet()) {
            Mat result = new Mat();
            Imgproc.matchTemplate(charImg, entry.getValue(), result, Imgproc.TM_SQDIFF_NORMED);
            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

            if (mmr.minVal < bestMatchScore) {
                bestMatchScore = mmr.minVal;
                bestMatch = entry.getKey();
            }
        }

        return bestMatch;
    }

    private boolean isValidPlateLength(int length) {
        return length >= 6 && length <= 10;
    }
}
