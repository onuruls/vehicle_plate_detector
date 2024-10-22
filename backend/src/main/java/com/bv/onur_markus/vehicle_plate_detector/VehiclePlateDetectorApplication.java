package com.bv.onur_markus.vehicle_plate_detector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.bv.onur_markus.vehicle_plate_detector.repository")
public class VehiclePlateDetectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(VehiclePlateDetectorApplication.class, args);
	}

}
