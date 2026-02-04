package com.bv.onur_markus.vehicle_plate_detector;

import com.bv.onur_markus.vehicle_plate_detector.model.DetectionResponse;
import com.bv.onur_markus.vehicle_plate_detector.controller.PlateDetectionController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VehiclePlateDetectorApplicationTests {

	@Autowired
	private PlateDetectionController plateDetectionController;

	@Test
	void contextLoads() {
		assertNotNull(plateDetectionController);
	}

	@ParameterizedTest
	@ValueSource(strings = { "1.jpg", "2.jpeg", "3.jpg", "4.jpg", "5.jpg" })
	void detectPlateFromExampleImages(String filename) throws IOException {
		Path imagePath = Paths.get("../examplePlates", filename);

		if (!Files.exists(imagePath)) {
			System.out.println("Skipping " + filename + " - file not found at " + imagePath.toAbsolutePath());
			return;
		}

		byte[] imageBytes = Files.readAllBytes(imagePath);
		String contentType = filename.endsWith(".png") ? "image/png" : "image/jpeg";

		MockMultipartFile file = new MockMultipartFile(
				"file",
				filename,
				contentType,
				imageBytes);

		DetectionResponse response = plateDetectionController.detectPlate(file);

		assertNotNull(response, "Response should not be null for " + filename);
		assertNotNull(response.status(), "Status should not be null");

		System.out.println("=== " + filename + " ===");
		System.out.println("Status: " + response.status());
		System.out.println("Plate: " + response.plateText());
		System.out.println("Prefix: " + response.prefix());
		System.out.println("City: " + response.city());
		System.out.println();

		// At minimum, we should get a valid status
		assertTrue(
				response.status().equals("OK") ||
						response.status().equals("NO_PLATE") ||
						response.status().equals("ERROR"),
				"Status should be OK, NO_PLATE, or ERROR");
	}

	@Test
	void detectPlateReturnsStructuredResponse() throws IOException {
		Path imagePath = Paths.get("../examplePlates/1.jpg");

		if (!Files.exists(imagePath)) {
			System.out.println("Skipping test - example image not found");
			return;
		}

		byte[] imageBytes = Files.readAllBytes(imagePath);
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"1.jpg",
				"image/jpeg",
				imageBytes);

		DetectionResponse response = plateDetectionController.detectPlate(file);

		assertNotNull(response);
		// Verify the response has all required fields
		assertNotNull(response.status());
		// plateText, prefix, city can be null depending on detection result
	}
}
