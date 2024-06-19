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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class PlateDetectionService {

    static {
        nu.pattern.OpenCV.loadShared();
    }

    private final ArrayList<CascadeClassifier> plateCascades = new ArrayList<>();
    private final ITesseract tesseract;

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
            int count = 0;

            // Convert MultipartFile to File
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
            file.transferTo(convFile);

            // Read image and preprocess
            Mat src = Imgcodecs.imread(convFile.getAbsolutePath());
            Mat gray = preprocessImage(src);
            Imgcodecs.imwrite("src/main/resources/temp_preprocessed.bmp", gray);

            // Detect plates
            List<Rect> detectedPlates = new ArrayList<>();
            int cascadeIndex = 0;
            while (detectedPlates.isEmpty() &&  cascadeIndex < plateCascades.size()) {
                detectPlates(gray, plateCascades.get(cascadeIndex), detectedPlates);
            }

            List<String> ocrResults = new ArrayList<>();
            for (Rect rect : detectedPlates) {
                count++;

                // Crop and save the detected plate
                Mat plate = new Mat(gray, rect);
                String platePath = String.format("src/main/resources/temp_plate_%s.bmp", count);
                Imgcodecs.imwrite(platePath, plate);

                // Perform OCR
                String result = performOCR(new File(platePath));
                result = filterOCRResult(result);
                ocrResults.add(result);
            }

            return String.join("\n", ocrResults);

        } catch (IOException | TesseractException e) {
            return "Error detecting plate";
        }
    }

    private void detectPlates(Mat image, CascadeClassifier classifier, List<Rect> detectedPlates) {
        MatOfRect plates = new MatOfRect();
        classifier.detectMultiScale(image, plates, 1.1, 5);
        detectedPlates.addAll(Arrays.asList(plates.toArray()));
    }

    private Mat preprocessImage(Mat src) {
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(gray, gray);
        Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);
        Imgproc.threshold(gray, gray, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        return gray;
    }

    private String performOCR(File imageFile) throws TesseractException {
        return tesseract.doOCR(imageFile);
    }

    private String filterOCRResult(String ocrResult) {
        // Use regular expressions to extract only letters, numbers, and spaces between groups
        StringBuilder filteredResult = new StringBuilder();
        Pattern pattern = Pattern.compile("[A-Za-z0-9]+");
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