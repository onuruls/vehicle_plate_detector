package com.bv.onur_markus.vehicle_plate_detector.controller;

import com.bv.onur_markus.vehicle_plate_detector.model.CityCode;
import com.bv.onur_markus.vehicle_plate_detector.service.CityCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/city-codes")
public class CityCodeController {

    @Autowired
    private CityCodeService cityCodeService;

    @GetMapping("/{code}")
    public CityCode getCityCodeByCode(@PathVariable String code) {
        return cityCodeService.getCityCodeByCode(code);
    }

    @PostMapping
    public CityCode addCityCode(@RequestBody CityCode cityCode) {
        return cityCodeService.addCityCode(cityCode);
    }
}
