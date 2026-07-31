package com.cards.auth.repository;

import com.cards.auth.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for {@link Role} entities.
 * Looks up roles by name when assigning defaults during registration.
 */
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * Finds a role by its unique name (for example {@code ROLE_USER}).
     *
     * @param name role name
     * @return the role if it exists
     */
    Optional<Role> findByName(String name);
}
