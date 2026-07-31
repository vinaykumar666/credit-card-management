package com.cards.enterprise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the enterprise API service.
 * Starts the adapter that authorizes payments against a simulated external network.
 */
@SpringBootApplication
public class EnterpriseApiServiceApplication {

    /**
     * Boots the enterprise API service.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(EnterpriseApiServiceApplication.class, args);
    }
}
