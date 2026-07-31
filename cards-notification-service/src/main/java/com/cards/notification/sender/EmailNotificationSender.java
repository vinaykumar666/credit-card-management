package com.cards.notification.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * {@link NotificationSender} for the EMAIL channel.
 * Currently logs the send attempt instead of calling a real email provider.
 */
@Component
public class EmailNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    /**
     * {@inheritDoc}
     *
     * @return {@code "EMAIL"}
     */
    @Override
    public String channel() {
        return "EMAIL";
    }

    /**
     * Logs an EMAIL send attempt for the given recipient and template.
     *
     * @param recipient destination email address
     * @param template  template key
     * @param payload   message payload
     */
    @Override
    public void send(String recipient, String template, String payload) {
        log.info("Sending EMAIL notification recipient={} template={} payload={}", recipient, template, payload);
    }
}
