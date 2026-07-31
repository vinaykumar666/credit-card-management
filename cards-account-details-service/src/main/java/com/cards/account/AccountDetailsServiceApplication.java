package com.cards.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the account details service.
 * Starts the application that manages credit-card account data and transaction history.
 */
@SpringBootApplication
public class AccountDetailsServiceApplication {

    /**
     * Boots the account details service.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(AccountDetailsServiceApplication.class, args);
    }
}
