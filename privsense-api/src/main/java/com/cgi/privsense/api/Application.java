package com.cgi.privsense.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application class for the PrivSense API.
 * Serves as the entry point for the API module and configures Spring Boot.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.cgi.privsense"})
@EnableCaching
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}