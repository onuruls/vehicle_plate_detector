package com.bv.onur_markus.vehicle_plate_detector.model;

/**
 * Represents a German license plate prefix and its corresponding city/district.
 */
public class CityCode {

    private String code;
    private String city;

    public CityCode() {
    }

    public CityCode(String code, String city) {
        this.code = code;
        this.city = city;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
