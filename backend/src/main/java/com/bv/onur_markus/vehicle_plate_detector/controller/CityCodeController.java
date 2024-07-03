package com.bv.onur_markus.vehicle_plate_detector.controller;

import com.bv.onur_markus.vehicle_plate_detector.model.CityCode;
import com.bv.onur_markus.vehicle_plate_detector.service.CityCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing city codes within the system.
 */
@RestController
@RequestMapping("/api/city-codes")
public class CityCodeController {

    @Autowired
    private CityCodeService cityCodeService;

    /**
     * Retrieves a city code based on its unique code identifier.
     * @param code The unique identifier for the city code.
     * @return The city code object corresponding to the provided identifier.
     */
    @GetMapping("/{code}")
    public CityCode getCityCodeByCode(@PathVariable String code) {
        return cityCodeService.getCityCodeByCode(code);
    }

    /**
     * Adds a new city code to the system.
     * @param cityCode The city code object to be added.
     * @return The newly added city code object.
     */
    @PostMapping
    public CityCode addCityCode(@RequestBody CityCode cityCode) {
        return cityCodeService.addCityCode(cityCode);
    }
}
