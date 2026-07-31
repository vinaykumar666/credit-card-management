package com.cards.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the API gateway service.
 * Starts the Spring Cloud Gateway that routes traffic to backend services.
 */
@SpringBootApplication
public class ApiGatewayServiceApplication {

    /**
     * Boots the API gateway service.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayServiceApplication.class, args);
    }
}
