package com.bv.onur_markus.vehicle_plate_detector.utils;

import nu.pattern.OpenCV;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
public class CharImageGenerator {

    static {
        OpenCV.loadLocally();
    }

    public static void main(String[] args) {
        String fontPath = "src/main/resources/fonts/CARGO2.TTF";
        int fontSize = 48;
        String outputDir = "src/main/resources/templates";

        // Create the templates directory
        new File(outputDir).mkdirs();

        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath)).deriveFont(Font.PLAIN, fontSize);

            // Generate images for digits 0-9
            for (int digit = 0; digit < 10; digit++) {
                BufferedImage img = createCharImage(String.valueOf(digit), font);
                invertColorsAndSave(img, outputDir + "/" + digit + ".png");
            }

            // Generate images for uppercase letters A-Z
            for (char c = 'A'; c <= 'Z'; c++) {
                BufferedImage img = createCharImage(String.valueOf(c), font);
                invertColorsAndSave(img, outputDir + "/" + c + ".png");
            }

            System.out.println("Templates generated and saved in 'templates' directory.");
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage createCharImage(String charStr, Font font) {
        int imgWidth = 48;
        int imgHeight = 48;
        BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = img.createGraphics();

        // Set white background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, imgWidth, imgHeight);

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