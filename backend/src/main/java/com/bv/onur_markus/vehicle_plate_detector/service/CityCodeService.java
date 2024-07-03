package com.bv.onur_markus.vehicle_plate_detector.service;

import com.bv.onur_markus.vehicle_plate_detector.model.CityCode;
import com.bv.onur_markus.vehicle_plate_detector.repository.CityCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * Service class for managing city codes.
 * Provides functionality to retrieve and add city codes to the database.
 */
@Service
public class CityCodeService {

    // Injecting the CityCodeRepository to interact with the database.
    @Autowired
    private CityCodeRepository cityCodeRepository;

    /**
     * Retrieves a city code by its code identifier.
     * Throws a 404 error if the city code is not found.
     *
     * @param code The code identifier of the city.
     * @return The CityCode object if found.
     */
    public CityCode getCityCodeByCode(String code) {
        return cityCodeRepository.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "City code not found"));
    }

    /**
     * Adds a new city code to the database.
     *
     * @param cityCode The CityCode object to be added.
     * @return The CityCode object after it has been saved to the database.
     */
    public CityCode addCityCode(CityCode cityCode) {
        return cityCodeRepository.save(cityCode);
    }
}
