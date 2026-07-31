package com.cards.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for a persisted refresh token in the {@code refresh_tokens} table.
 * Stores only a hash of the token, its expiry, revoke flag, and owning user.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    /** Primary key; generated on first persist if unset. */
    @Id
    private UUID id;

    /** User who owns this refresh token. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** SHA-256 hex hash of the opaque refresh token value. */
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    /** Instant after which the token is no longer valid. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** When true, the token must not be accepted for refresh. */
    @Column(nullable = false)
    private boolean revoked;

    /** Creation timestamp; set automatically on first persist. */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Assigns id and created-at before the entity is first saved.
     */
    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /**
     * Indicates whether the current time is past {@link #expiresAt}.
     *
     * @return {@code true} if the token has expired
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
