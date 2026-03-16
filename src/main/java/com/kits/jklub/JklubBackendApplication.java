package com.kits.jklub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application entry point.
 * This file bootstraps the Spring Boot application and starts the embedded Tomcat server.
 */
@SpringBootApplication
public class JklubBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(JklubBackendApplication.class, args);
        System.out.println("\n*** Jeevan Kaushal Club Backend Started on http://localhost:8080 ***\n");
    }
}
