package com.bv.onur_markus.vehicle_plate_detector.controller;

import com.bv.onur_markus.vehicle_plate_detector.model.CityCode;
import com.bv.onur_markus.vehicle_plate_detector.service.CityCodeService;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for city code lookups.
 */
@RestController
@RequestMapping("/api/city-codes")
public class CityCodeController {

    private final CityCodeService cityCodeService;

    public CityCodeController(CityCodeService cityCodeService) {
        this.cityCodeService = cityCodeService;
    }

    /**
     * Look up a city by its license plate prefix.
     * @param code The German license plate prefix (e.g., "B", "HH", "KI")
     * @return The city code with city name
     */
    @GetMapping("/{code}")
    public CityCode getCityCodeByCode(@PathVariable String code) {
        return cityCodeService.requireByCode(code);
    }
}
