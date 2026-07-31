package com.cards.notification.web;

import com.cards.common.error.ErrorCodes;
import com.cards.common.error.NotFoundException;
import com.cards.notification.repository.NotificationLogRepository;
import com.cards.notification.web.dto.NotificationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for reading notification delivery logs.
 * Exposes {@code /api/v1/notifications} lookup by id and by user.
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationLogRepository notificationLogRepository;

    /**
     * Creates the controller with the notification log repository.
     *
     * @param notificationLogRepository repository used for lookups
     */
    public NotificationController(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    /**
     * Returns one notification log by id.
     *
     * @param id notification log identifier
     * @return notification details
     * @throws NotFoundException if no log exists for the id
     */
    @GetMapping("/{id}")
    public NotificationResponse getById(@PathVariable UUID id) {
        return notificationLogRepository.findById(id)
                .map(NotificationResponse::from)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.NOTIF_001));
    }

    /**
     * Returns all notification logs for a user, newest first.
     *
     * @param userId user identifier
     * @return list of notification responses (may be empty)
     */
    @GetMapping("/user/{userId}")
    public List<NotificationResponse> getByUserId(@PathVariable UUID userId) {
        return notificationLogRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }
}
