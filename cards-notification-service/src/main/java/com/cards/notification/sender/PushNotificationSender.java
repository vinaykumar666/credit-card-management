package com.cards.notification.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * {@link NotificationSender} for the PUSH channel.
 * Currently logs the send attempt instead of calling a real push provider.
 */
@Component
public class PushNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationSender.class);

    /**
     * {@inheritDoc}
     *
     * @return {@code "PUSH"}
     */
    @Override
    public String channel() {
        return "PUSH";
    }

    /**
     * Logs a PUSH send attempt for the given recipient and template.
     *
     * @param recipient destination device or push token
     * @param template  template key
     * @param payload   message payload
     */
    @Override
    public void send(String recipient, String template, String payload) {
        log.info("Sending PUSH notification recipient={} template={} payload={}", recipient, template, payload);
    }
}
