package com.bv.onur_markus.vehicle_plate_detector.repository;

import com.bv.onur_markus.vehicle_plate_detector.model.CityCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface CityCodeRepository extends MongoRepository<CityCode, String> {
    Optional<CityCode> findByCode(String code);
}
