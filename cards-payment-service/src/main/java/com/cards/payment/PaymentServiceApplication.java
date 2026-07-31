package com.cards.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the cards payment service.
 * Starts the application context that handles payment initiation, strategies, and related APIs.
 */
@SpringBootApplication
public class PaymentServiceApplication {

    /**
     * Boots the payment service.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
