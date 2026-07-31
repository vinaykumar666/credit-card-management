package com.cards.notification.sender;

/**
 * Contract for channel-specific notification delivery.
 * Every implementation must report its channel name and honor the same send semantics
 * so callers can switch channels without knowing the concrete type.
 */
public interface NotificationSender {

    /**
     * Returns the channel this sender handles (for example, EMAIL, SMS, or PUSH).
     *
     * @return channel name used for lookup
     */
    String channel();

    /**
     * Sends a notification to the given recipient using the named template and payload.
     *
     * @param recipient destination address or device target
     * @param template  template key for the message
     * @param payload   message body or serialized placeholders
     */
    void send(String recipient, String template, String payload);
}
