package com.bv.onur_markus.vehicle_plate_detector.utils;

import nu.pattern.OpenCV;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Standalone utility for generating character template images.
 * Run this class directly to regenerate templates.
 * 
 * Usage: java CharImageGenerator [outputDir]
 * Default output: ./generated-templates
 */
public class CharImageGenerator {

    static {
        OpenCV.loadLocally();
    }

    public static void main(String[] args) {
        String outputDir = args.length > 0 ? args[0] : "./generated-templates";
        int fontSize = 48;

        // Create the base templates directory
        new File(outputDir).mkdirs();

        // Load system font
        Font font = loadFont(fontSize);

        // Generate images for digits 0-9
        for (int digit = 0; digit < 10; digit++) {
            generateRotatedImages(String.valueOf(digit), font, outputDir);
        }

        // Generate images for uppercase letters A-Z
        for (char c = 'A'; c <= 'Z'; c++) {
            generateRotatedImages(String.valueOf(c), font, outputDir);
        }

        System.out.println("Templates generated and saved in '" + outputDir + "' directory.");
        System.out.println("Copy these to src/main/resources/templates/ if needed.");
    }

    private static Font loadFont(int fontSize) {
        // Use a system monospace font (universally available, no license issues)
        return new Font(Font.MONOSPACED, Font.BOLD, fontSize);
    }

    private static void generateRotatedImages(String charStr, Font font, String baseOutputDir) {
        String charDir = baseOutputDir + "/" + charStr;
        new File(charDir).mkdirs();

        for (int angle = -20; angle <= 20; angle += 5) {
            BufferedImage img = createCharImage(charStr, font, angle);
            invertColorsAndSave(img, charDir + "/" + charStr + "_" + angle + ".png");
        }
    }

    private static BufferedImage createCharImage(String charStr, Font font, int angle) {
        int imgWidth = 48;
        int imgHeight = 48;
        BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = img.createGraphics();

        // Set white background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, imgWidth, imgHeight);

        // Apply rotation
        AffineTransform origTransform = g2d.getTransform();
        AffineTransform rotTransform = AffineTransform.getRotateInstance(Math.toRadians(angle), imgWidth / 2.0,
                imgHeight / 2.0);
        g2d.setTransform(rotTransform);

        // Set black text
        g2d.setColor(Color.BLACK);
        g2d.setFont(font);

        // Calculate position to center the text
        FontRenderContext frc = g2d.getFontRenderContext();
        GlyphVector gv = font.createGlyphVector(frc, charStr);
        Rectangle textBounds = gv.getPixelBounds(frc, 0, 0);
        int textX = (imgWidth - textBounds.width) / 2;
        int textY = (imgHeight - textBounds.height) / 2 + textBounds.height;

        // Draw the character
        g2d.drawString(charStr, textX, textY);
        g2d.setTransform(origTransform);
        g2d.dispose();

        return img;
    }

    private static void invertColorsAndSave(BufferedImage img, String outputPath) {
        // Convert BufferedImage to Mat
        Mat mat = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC1);
        byte[] data = ((java.awt.image.DataBufferByte) img.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);

        // Invert colors
        Core.bitwise_not(mat, mat);

        // Convert Mat back to BufferedImage
        byte[] invertedData = new byte[mat.rows() * mat.cols() * (int) (mat.elemSize())];
        mat.get(0, 0, invertedData);
        BufferedImage invertedImg = new BufferedImage(mat.cols(), mat.rows(), BufferedImage.TYPE_BYTE_GRAY);
        invertedImg.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), invertedData);

        // Save image
        try {
            ImageIO.write(invertedImg, "png", new File(outputPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}