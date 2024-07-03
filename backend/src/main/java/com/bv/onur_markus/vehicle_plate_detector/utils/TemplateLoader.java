package com.bv.onur_markus.vehicle_plate_detector.utils;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TemplateLoader {
    private final Map<Character, Mat> templates = new HashMap<>();

    public TemplateLoader() {
        loadTemplates();
    }

    private void loadTemplates() {
        String templateDir = "src/main/resources/templates";
        File dir = new File(templateDir);

        if (dir.exists() && dir.isDirectory()) {
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                if (file.isFile() && file.getName().endsWith(".png")) {
                    String name = file.getName().replace(".png", "");
                    char templateChar = name.charAt(0);
                    Mat template = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
                    templates.put(templateChar, template);
                }
            }
        } else {
            throw new RuntimeException("Template directory not found: " + templateDir);
        }
    }

    public Map<Character, Mat> getTemplates() {
        return templates;
    }
}
