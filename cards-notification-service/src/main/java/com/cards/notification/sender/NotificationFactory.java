package com.cards.notification.sender;

import com.cards.common.error.ErrorCodes;
import com.cards.common.error.ValidationBusinessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory that resolves a {@link NotificationSender} by channel name.
 * Indexes all Spring-managed senders at startup for case-insensitive lookup.
 */
@Component
public class NotificationFactory {

    private final Map<String, NotificationSender> sendersByChannel;

    /**
     * Builds a channel-to-sender map from all available {@link NotificationSender} beans.
     *
     * @param senders all registered notification senders
     */
    public NotificationFactory(List<NotificationSender> senders) {
        this.sendersByChannel = senders.stream()
                .collect(Collectors.toMap(
                        sender -> sender.channel().toUpperCase(Locale.ROOT),
                        Function.identity()
                ));
    }

    /**
     * Returns the sender for the given channel.
     *
     * @param channel channel name such as EMAIL, SMS, or PUSH
     * @return matching notification sender
     * @throws ValidationBusinessException if the channel is blank or unsupported
     */
    public NotificationSender getSender(String channel) {
        if (channel == null || channel.isBlank()) {
            throw new ValidationBusinessException(ErrorCodes.NOTIF_002, "Notification channel is required");
        }
        NotificationSender sender = sendersByChannel.get(channel.toUpperCase(Locale.ROOT));
        if (sender == null) {
            throw new ValidationBusinessException(ErrorCodes.NOTIF_002, "Unsupported notification channel: " + channel);
        }
        return sender;
    }
}
