package com.elitecart.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * EliteCart - Professional E-Commerce Platform.
 * Entry point of the Spring Boot application.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class EliteCartBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EliteCartBackendApplication.class, args);
    }
}
