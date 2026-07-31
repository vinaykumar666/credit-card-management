package com.cards.notification.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * {@link NotificationSender} for the SMS channel.
 * Currently logs the send attempt instead of calling a real SMS provider.
 */
@Component
public class SmsNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationSender.class);

    /**
     * {@inheritDoc}
     *
     * @return {@code "SMS"}
     */
    @Override
    public String channel() {
        return "SMS";
    }

    /**
     * Logs an SMS send attempt for the given recipient and template.
     *
     * @param recipient destination phone number
     * @param template  template key
     * @param payload   message payload
     */
    @Override
    public void send(String recipient, String template, String payload) {
        log.info("Sending SMS notification recipient={} template={} payload={}", recipient, template, payload);
    }
}
