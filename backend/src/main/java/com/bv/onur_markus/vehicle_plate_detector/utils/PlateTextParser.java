package com.bv.onur_markus.vehicle_plate_detector.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for parsing German license plate text to extract the city prefix.
 * 
 * German plates have the format: PREFIX LETTERS NUMBERS
 * Examples:
 *   "K-AB 1234" -> prefix "K" (Köln)
 *   "HH AB 123" -> prefix "HH" (Hamburg)
 *   "KI-XY 12"  -> prefix "KI" (Kiel)
 *   "B AB 1234" -> prefix "B" (Berlin)
 */
public class PlateTextParser {

    private static final Pattern PREFIX_PATTERN = Pattern.compile("^([A-ZÄÖÜ]{1,3})");

    private PlateTextParser() {
    }

    /**
     * Extract the German city prefix from a detected plate text.
     * Takes the first token (split on space/hyphen) and extracts leading letters.
     * 
     * @param plateText The detected plate text
     * @return The uppercase prefix, or null if none found
     */
    public static String extractPrefix(String plateText) {
        if (plateText == null || plateText.isBlank()) {
            return null;
        }

        String normalized = plateText.toUpperCase().trim();
        
        // Split on first space or hyphen to get prefix token
        String[] parts = normalized.split("[\\s\\-]+", 2);
        if (parts.length == 0 || parts[0].isEmpty()) {
            return null;
        }

        String firstToken = parts[0];
        
        // Extract leading alphabetic characters (1-3 letters for German plates)
        Matcher matcher = PREFIX_PATTERN.matcher(firstToken);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}
