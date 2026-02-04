package com.bv.onur_markus.vehicle_plate_detector.service;

import com.bv.onur_markus.vehicle_plate_detector.model.CityCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for looking up German license plate prefixes to city/district names.
 * Data is loaded from city_codes.json at startup.
 */
@Service
public class CityCodeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CityCodeService.class);
    private Map<String, CityCode> cityCodeMap = Collections.emptyMap();

    @PostConstruct
    public void init() {
        loadCityCodes();
    }

    private void loadCityCodes() {
        try {
            ClassPathResource resource = new ClassPathResource("city_codes.json");
            try (InputStream is = resource.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                List<CityCode> codes = mapper.readValue(is, new TypeReference<List<CityCode>>() {});
                cityCodeMap = codes.stream()
                        .collect(Collectors.toMap(
                                c -> c.getCode().toUpperCase(),
                                Function.identity(),
                                (existing, replacement) -> existing
                        ));
                LOGGER.info("Loaded {} city codes from city_codes.json", cityCodeMap.size());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load city_codes.json", e);
            throw new RuntimeException("Failed to load city codes", e);
        }
    }

    /**
     * Find a city code by its prefix.
     * @param code The license plate prefix (e.g., "B", "HH", "KI")
     * @return Optional containing the CityCode if found
     */
    public Optional<CityCode> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(cityCodeMap.get(code.toUpperCase()));
    }

    /**
     * Get a city code by its prefix, throwing 404 if not found.
     * @param code The license plate prefix
     * @return The CityCode
     * @throws ResponseStatusException with 404 status if not found
     */
    public CityCode requireByCode(String code) {
        return findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "City code not found: " + code
                ));
    }

    /**
     * Get the total number of loaded city codes.
     */
    public int getCodeCount() {
        return cityCodeMap.size();
    }
}
