package com.bv.onur_markus.vehicle_plate_detector.controller;

import com.bv.onur_markus.vehicle_plate_detector.service.PlateDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class PlateDetectionController {

    @Autowired
    private PlateDetectionService plateDetectionService;

    @PostMapping("/detect")
    public String detectPlate(@RequestParam("file") MultipartFile file) {
        return plateDetectionService.detectPlate(file);
    }
}