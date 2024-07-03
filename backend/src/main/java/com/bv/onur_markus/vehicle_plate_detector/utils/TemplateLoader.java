package com.bv.onur_markus.vehicle_plate_detector.utils;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TemplateLoader {
    private final Map<Character, Mat> templates = new HashMap<>();

    /**
     * Constructor that initializes the template loading process.
     */
    public TemplateLoader() {
        loadTemplates(); // Load templates on initialization.
    }

    /**
     * Loads character templates from a specified directory into a map for easy access.
     */
    private void loadTemplates() {
        String templateDir = "src/main/resources/templates";
        File dir = new File(templateDir);

        // Check if the directory exists and is a directory
        if (dir.exists() && dir.isDirectory()) {
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                if (file.isFile() && file.getName().endsWith(".png")) {
                    // Extract the character from the file name
                    String name = file.getName().replace(".png", "");
                    char templateChar = name.charAt(0);
                    // Read the image as a grayscale matrix
                    Mat template = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
                    templates.put(templateChar, template); // Store the template in the map
                }
            }
        } else {
            throw new RuntimeException("Template directory not found: " + templateDir); // Handle the case where the directory is not found
        }
    }

    /**
     * Returns the map of character templates.
     * @return A map where each character is associated with its template image.
     */
    public Map<Character, Mat> getTemplates() {
        return templates;
    }
}
