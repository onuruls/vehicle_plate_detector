package com.bv.onur_markus.vehicle_plate_detector.model;

/**
 * Response DTO for plate detection API endpoints.
 */
public record DetectionResponse(
        String plateText,
        String prefix,
        String city,
        String status,
        String error
) {
    public static DetectionResponse ok(String plateText, String prefix, String city) {
        return new DetectionResponse(plateText, prefix, city, "OK", null);
    }

    public static DetectionResponse noPlate() {
        return new DetectionResponse(null, null, null, "NO_PLATE", "No license plate detected");
    }

    public static DetectionResponse error(String message) {
        return new DetectionResponse(null, null, null, "ERROR", message);
    }
}
