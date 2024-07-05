package com.bv.onur_markus.vehicle_plate_detector.utils;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TemplateLoader {
    private final Map<Character, List<Mat>> templates = new HashMap<>();

    public TemplateLoader() {
        loadTemplates();
    }

    private void loadTemplates() {
        String templateDir = "src/main/resources/templates";
        File dir = new File(templateDir);

        if (dir.exists() && dir.isDirectory()) {
            for (File subdir : Objects.requireNonNull(dir.listFiles())) {
                if (subdir.isDirectory()) {
                    char templateChar = subdir.getName().charAt(0);
                    List<Mat> templateVariants = new ArrayList<>();
                    for (File file : Objects.requireNonNull(subdir.listFiles())) {
                        if (file.isFile() && file.getName().endsWith(".png")) {
                            Mat template = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
                            templateVariants.add(template);
                        }
                    }
                    templates.put(templateChar, templateVariants);
                }
            }
        } else {
            throw new RuntimeException("Template directory not found: " + templateDir);
        }
    }

    public Map<Character, List<Mat>> getTemplates() {
        return templates;
    }
}
