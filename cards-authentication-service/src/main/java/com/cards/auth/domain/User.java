package com.cards.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA entity for an application user in the {@code users} table.
 * Stores credentials, profile fields, enablement flag, and many-to-many roles via {@code user_roles}.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /** Primary key; generated on first persist if unset. */
    @Id
    private UUID id;

    /** Unique login email (stored lower-cased on registration). */
    @Column(nullable = false, unique = true)
    private String email;

    /** Encoded password hash (never store plain text). */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** Display name of the user. */
    @Column(name = "full_name", nullable = false)
    private String fullName;

    /** When false, the user cannot log in. */
    @Column(nullable = false)
    private boolean enabled;

    /** Creation timestamp; set on first persist. */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Last update timestamp; refreshed on every update. */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Roles granted to this user (loaded eagerly for token issuance). */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * Assigns id and timestamps before the entity is first saved.
     */
    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * Updates {@link #updatedAt} before each database update.
     */
    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
