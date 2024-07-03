package com.bv.onur_markus.vehicle_plate_detector.controller;

import com.bv.onur_markus.vehicle_plate_detector.service.PlateDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for handling requests related to license plate detection.
 */
@RestController
@RequestMapping("/api")
public class PlateDetectionController {

    @Autowired
    private PlateDetectionService plateDetectionService;

    /**
     * Endpoint to detect a license plate from a uploaded file image.
     * @param file The MultipartFile containing the image of the license plate.
     * @return The detected plate's text or an error message if detection fails.
     */
    @PostMapping("/detect")
    public String detectPlate(@RequestParam("file") MultipartFile file) {
        return plateDetectionService.detectPlate(file);
    }

    /**
     * Endpoint to detect a license plate from a base64 encoded image string.
     * @param base64Image The base64 encoded string of the license plate image.
     * @return The detected plate's text or an error message if detection fails.
     */
    @PostMapping("/detect-base64")
    public String detectPlateFromBase64(@RequestBody String base64Image) {
        return plateDetectionService.detectPlateFromBase64(base64Image);
    }
}
