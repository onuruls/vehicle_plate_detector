package com.bv.onur_markus.vehicle_plate_detector.service;

import com.bv.onur_markus.vehicle_plate_detector.model.CityCode;
import com.bv.onur_markus.vehicle_plate_detector.repository.CityCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class CityCodeService {

    @Autowired
    private CityCodeRepository cityCodeRepository;

    public CityCode getCityCodeByCode(String code) {
        return cityCodeRepository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "City code not found"));
    }

    public CityCode addCityCode(CityCode cityCode) {
        return cityCodeRepository.save(cityCode);
    }
}
