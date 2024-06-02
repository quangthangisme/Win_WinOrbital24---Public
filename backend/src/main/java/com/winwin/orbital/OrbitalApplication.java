package com.winwin.orbital;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class OrbitalApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrbitalApplication.class, args);
	}

}
