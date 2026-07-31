package com.cards.notification.repository;

import com.cards.notification.domain.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for {@link NotificationLog} persistence.
 * Supports listing a user's notifications newest first.
 */
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    /**
     * Finds notification logs for a user ordered by creation time descending.
     *
     * @param userId user identifier
     * @return matching logs, newest first (may be empty)
     */
    List<NotificationLog> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
