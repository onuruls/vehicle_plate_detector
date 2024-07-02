package com.bv.onur_markus.vehicle_plate_detector.service;

import net.sourceforge.tess4j.ITessAPI;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

@Service
public class PlateDetectionService {

    static {
        nu.pattern.OpenCV.loadShared();
    }

    private final ITesseract tesseract;
    private static final Logger LOGGER = LoggerFactory.getLogger(PlateDetectionService.class);

    public PlateDetectionService() {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath("src/main/resources/tessdatas");
        this.tesseract.setLanguage("deu");
        this.tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SINGLE_LINE);
    }

    public String detectPlate(MultipartFile file) {
        try {
            // Convert MultipartFile to File
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
            file.transferTo(convFile);
            return detectPlateFromFile(convFile);
        } catch (IOException e) {
            LOGGER.error("Error processing file", e);
            return "Error processing file";
        }
    }

    public String detectPlateFromBase64(String base64Image) {
        try {
            // Decode Base64 image
            String[] parts = base64Image.split(",");
            if (parts.length != 2) {
                return "Invalid base64 image format";
            }
            String imageString = parts[1];
            byte[] decodedBytes = Base64.getDecoder().decode(imageString);

            // Convert decoded bytes to BufferedImage
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(decodedBytes));
            File imageFile = new File("received_image.png");
            ImageIO.write(bufferedImage, "png", imageFile);

            return detectPlateFromFile(imageFile);
        } catch (IOException e) {
            LOGGER.error("Error decoding image", e);
            return "Error decoding image";
        }
    }

    private String detectPlateFromFile(File file) {
        try {
            // Read the image file
            Mat src = Imgcodecs.imread(file.getAbsolutePath());
            Mat gray = new Mat();

            // Convert to grayscale
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
            Imgcodecs.imwrite("src/main/resources/gray_image.png", gray);

            // Apply Gaussian blur to reduce noise
            Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
            Imgproc.equalizeHist(gray, gray);

            // Apply blackhat morphology to highlight dark regions on a light background
            Mat blackhat = new Mat();
            Mat rectKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(13, 5));
            Imgproc.morphologyEx(gray, blackhat, Imgproc.MORPH_BLACKHAT, rectKernel);
            Imgcodecs.imwrite("src/main/resources/blackhat_image.png", blackhat);

            // Apply adaptive thresholding to get a binary image
            Mat thresh = new Mat();
            Imgproc.adaptiveThreshold(blackhat, thresh, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 25, 15);
            Imgcodecs.imwrite("src/main/resources/thresh_image.png", thresh);

            // Detect possible plates from the thresholded image
            List<Rect> possiblePlates = detectPossiblePlates(thresh);

            // If any plates are detected, perform OCR on the best candidate
            if (!possiblePlates.isEmpty()) {
                Rect bestPlate = possiblePlates.get(0);
                Mat plate = new Mat(src, bestPlate);
                String platePath = "src/main/resources/temp_plate.bmp";
                Imgcodecs.imwrite(platePath, plate);
                String ocrResult = performOCR(new File(platePath));
                return filterOCRResult(ocrResult);
            }

            return "Not found";

        } catch (TesseractException e) {
            LOGGER.error("Error detecting plate", e);
            return "Error detecting plate";
        }
    }

    private List<Rect> detectPossiblePlates(Mat preprocessedImage) {
        List<Rect> possiblePlates = new ArrayList<>();
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        // Find contours in the preprocessed image
        Imgproc.findContours(preprocessedImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            double aspectRatio = (double) rect.width / rect.height;
            double area = Imgproc.contourArea(contour);

            // Filter contours by aspect ratio and area to find possible plates
            if (aspectRatio >= 2 && aspectRatio <= 6 && area > 100) {
                possiblePlates.add(rect);
            }
        }

        return possiblePlates;
    }

    private String performOCR(File imageFile) throws TesseractException {
        // Perform OCR using Tesseract
        return tesseract.doOCR(imageFile);
    }

    private String filterOCRResult(String ocrResult) {
        // Filter OCR results to keep only alphanumeric characters
        StringBuilder filteredResult = new StringBuilder();
        Pattern pattern = Pattern.compile("[A-Z0-9]+");
        Matcher matcher = pattern.matcher(ocrResult);
        while (matcher.find()) {
            filteredResult.append(matcher.group()).append(" ");
        }
        return filteredResult.toString().trim();
    }
}
