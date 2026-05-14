package com.diveconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DiveconnectApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiveconnectApplication.class, args);
	}

}
