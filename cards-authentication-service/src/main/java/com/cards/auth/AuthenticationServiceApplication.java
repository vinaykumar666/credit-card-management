package com.cards.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the cards authentication service.
 * Starts the application that handles user login, registration, token refresh, and OAuth2 authorization.
 */
@SpringBootApplication
public class AuthenticationServiceApplication {

    /**
     * Boots the authentication service.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthenticationServiceApplication.class, args);
    }
}
