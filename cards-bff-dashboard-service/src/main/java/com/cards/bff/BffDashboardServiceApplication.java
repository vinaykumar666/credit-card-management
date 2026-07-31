package com.cards.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the BFF dashboard service.
 * Starts the application that aggregates account, payment, and notification data for UI clients.
 */
@SpringBootApplication
public class BffDashboardServiceApplication {

    /**
     * Boots the BFF dashboard service.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(BffDashboardServiceApplication.class, args);
    }
}
