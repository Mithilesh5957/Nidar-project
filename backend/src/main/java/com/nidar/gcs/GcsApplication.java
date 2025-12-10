package com.nidar.gcs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GcsApplication {

	public static void main(String[] args) {
		SpringApplication.run(GcsApplication.class, args);
	}

}
