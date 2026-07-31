package com.cards.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the notification service.
 * Starts the application that dispatches email, SMS, and push notifications and stores delivery logs.
 */
@SpringBootApplication
public class NotificationServiceApplication {

    /**
     * Boots the notification service.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
