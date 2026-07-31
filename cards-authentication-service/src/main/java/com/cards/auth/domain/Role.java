package com.cards.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * JPA entity for an authorization role in the {@code roles} table.
 * Roles (for example {@code ROLE_USER}) are assigned to users and embedded in JWT claims.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    /** Primary key of the role. */
    @Id
    private UUID id;

    /** Unique role name used in authorization checks and JWT claims. */
    @Column(nullable = false, unique = true)
    private String name;
}
