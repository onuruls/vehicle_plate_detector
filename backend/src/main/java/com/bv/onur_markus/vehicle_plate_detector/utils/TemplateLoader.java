package com.bv.onur_markus.vehicle_plate_detector.utils;

import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Loads character templates from classpath resources.
 * Works both in IDE and packaged JAR.
 */
@Component
public class TemplateLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateLoader.class);
    private final Map<Character, List<Mat>> templates = new HashMap<>();

    static {
        OpenCV.loadLocally();
    }

    @PostConstruct
    public void init() {
        loadTemplates();
    }

    private void loadTemplates() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:templates/*/*.png");

            for (Resource resource : resources) {
                String path = resource.getURL().getPath();
                // Extract character from parent directory name
                String[] pathParts = path.split("/");
                if (pathParts.length >= 2) {
                    String charDirName = pathParts[pathParts.length - 2];
                    if (!charDirName.isEmpty()) {
                        char templateChar = charDirName.charAt(0);
                        Mat template = loadMatFromResource(resource);
                        if (template != null && !template.empty()) {
                            templates.computeIfAbsent(templateChar, k -> new ArrayList<>()).add(template);
                        }
                    }
                }
            }

            LOGGER.info("Loaded templates for {} characters", templates.size());
        } catch (IOException e) {
            LOGGER.error("Failed to load templates from classpath", e);
            throw new RuntimeException("Failed to load character templates", e);
        }
    }

    private Mat loadMatFromResource(Resource resource) {
        try (InputStream is = resource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            MatOfByte mob = new MatOfByte(bytes);
            return Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_GRAYSCALE);
        } catch (IOException e) {
            LOGGER.warn("Failed to load template: {}", resource.getFilename(), e);
            return null;
        }
    }

    public Map<Character, List<Mat>> getTemplates() {
        return templates;
    }
}
