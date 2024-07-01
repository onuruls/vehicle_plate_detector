package com.bv.onur_markus.vehicle_plate_detector.service;

import net.sourceforge.tess4j.ITessAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

@Service
public class PlateDetectionService {

    static {
        nu.pattern.OpenCV.loadShared();
    }

    private final ArrayList<CascadeClassifier> plateCascades = new ArrayList<>();
    private final ITesseract tesseract;

    private static final Logger LOGGER = LoggerFactory.getLogger(PlateDetectionService.class);

    public PlateDetectionService() {
        this.plateCascades.add(new CascadeClassifier("src/main/resources/haarcascades/haarcascade_russian_plate_number.xml"));
        this.plateCascades.add(new CascadeClassifier("src/main/resources/haarcascades/haarcascade_licence_plate_rus_16stages.xml"));
        this.plateCascades.add(new CascadeClassifier("src/main/resources/haarcascades/haarcascades_indian_license_plate.xml"));
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath("src/main/resources/tessdatas");
        this.tesseract.setLanguage("deu");
        this.tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_SINGLE_LINE);
    }

    public String detectPlate(MultipartFile file) {
        try {
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
            String[] parts = base64Image.split(",");
            if (parts.length != 2) {
                return "Invalid base64 image format";
            }
            String imageString = parts[1];
            byte[] decodedBytes = Base64.getDecoder().decode(imageString);

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
            // Read image and preprocess
            Mat src = Imgcodecs.imread(file.getAbsolutePath());
            Mat processedImage = preprocessImage(src);
            Imgcodecs.imwrite("src/main/resources/processed_image1.bmp", processedImage);

            // Detect plates
            List<Rect> detectedPlates = new ArrayList<>();
            int cascadeIndex = 0;
            while (detectedPlates.isEmpty() && cascadeIndex < plateCascades.size()) {
                detectPlates(processedImage, plateCascades.get(cascadeIndex), detectedPlates);
                cascadeIndex++;
            }

            if (detectedPlates.isEmpty()) {
                return "Not found";
            }

            List<String> ocrResults = new ArrayList<>();
            for (Rect rect : detectedPlates) {
                // Crop and save the detected plate
                Mat plate = new Mat(processedImage, rect);
                String platePath = "src/main/resources/temp_plate.bmp";
                Imgcodecs.imwrite(platePath, plate);

                // Perform OCR
                String result = performOCR(new File(platePath));
                result = filterOCRResult(result);
                ocrResults.add(result);
            }
            //TODO: Check if the detected plate is valid or string is nonEmpty
            return String.join("\n", ocrResults);

        } catch (TesseractException e) {
            LOGGER.error("Error detecting plate", e);
            return "Error detecting plate";
        }
    }

    private Mat preprocessImage(Mat src) {
        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        // Enhance contrast using histogram equalization
        Imgproc.equalizeHist(gray, gray);

        // Apply bilateral filter to reduce noise while keeping edges sharp
        Mat bilateral = new Mat();
        Imgproc.bilateralFilter(gray, bilateral, 9, 75, 75);

        // Apply adaptive thresholding
        Mat adaptiveThreshold = new Mat();
        Imgproc.adaptiveThreshold(bilateral, adaptiveThreshold, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);

        // Morphological operations to close gaps in detected edges
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Mat morph = new Mat();
        Imgproc.morphologyEx(adaptiveThreshold, morph, Imgproc.MORPH_CLOSE, kernel);
        Imgproc.morphologyEx(morph, morph, Imgproc.MORPH_OPEN, kernel);

        return morph;
    }

    private void detectPlates(Mat image, CascadeClassifier classifier, List<Rect> detectedPlates) {
        MatOfRect plates = new MatOfRect();
        classifier.detectMultiScale(image, plates, 1.1, 5);
        detectedPlates.addAll(Arrays.asList(plates.toArray()));
    }

    private String performOCR(File imageFile) throws TesseractException {
        return tesseract.doOCR(imageFile);
    }

    private String filterOCRResult(String ocrResult) {
        StringBuilder filteredResult = new StringBuilder();
        Pattern pattern = Pattern.compile("[A-Z0-9]+");
        Matcher matcher = pattern.matcher(ocrResult);
        boolean first = true;
        while (matcher.find()) {
            if (!first) {
                filteredResult.append(" ");
            }
            filteredResult.append(matcher.group());
            first = false;
        }
        return filteredResult.toString();
    }
}
