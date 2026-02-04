package com.bv.onur_markus.vehicle_plate_detector.controller;

import com.bv.onur_markus.vehicle_plate_detector.model.CityCode;
import com.bv.onur_markus.vehicle_plate_detector.model.DetectionResponse;
import com.bv.onur_markus.vehicle_plate_detector.service.CityCodeService;
import com.bv.onur_markus.vehicle_plate_detector.service.PlateDetectionService;
import com.bv.onur_markus.vehicle_plate_detector.utils.PlateTextParser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for license plate detection endpoints.
 */
@RestController
@RequestMapping("/api")
public class PlateDetectionController {

    private final PlateDetectionService plateDetectionService;
    private final CityCodeService cityCodeService;

    public PlateDetectionController(PlateDetectionService plateDetectionService, CityCodeService cityCodeService) {
        this.plateDetectionService = plateDetectionService;
        this.cityCodeService = cityCodeService;
    }

    /**
     * Detect a license plate from an uploaded image file.
     * 
     * @param file The image file containing the license plate
     * @return Detection result with plate text, prefix, and city
     */
    @PostMapping("/detect")
    public DetectionResponse detectPlate(@RequestParam("file") MultipartFile file) {
        String plateText = plateDetectionService.detectPlate(file);
        return buildResponse(plateText);
    }

    /**
     * Detect a license plate from a base64 encoded image.
     * 
     * @param base64Image The base64 encoded image string
     * @return Detection result with plate text, prefix, and city
     */
    @PostMapping("/detect-base64")
    public DetectionResponse detectPlateFromBase64(@RequestBody String base64Image) {
        String plateText = plateDetectionService.detectPlateFromBase64(base64Image);
        return buildResponse(plateText);
    }

    private DetectionResponse buildResponse(String plateText) {
        if (plateText == null || plateText.equals("Not found")) {
            return DetectionResponse.noPlate();
        }

        if (plateText.startsWith("Error")) {
            return DetectionResponse.error(plateText);
        }

        String prefix = PlateTextParser.extractPrefix(plateText);
        String city = null;

        if (prefix != null) {
            city = cityCodeService.findByCode(prefix)
                    .map(CityCode::getCity)
                    .orElse(null);
        }

        return DetectionResponse.ok(plateText, prefix, city);
    }
}
