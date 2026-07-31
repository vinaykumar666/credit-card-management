package com.cards.auth.repository;

import com.cards.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for {@link User} entities.
 * Supports case-insensitive email lookup and existence checks for login and registration.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by email, ignoring letter case.
     *
     * @param email email address to search for
     * @return the user if found
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Checks whether any user already has the given email (case-insensitive).
     *
     * @param email email address to check
     * @return {@code true} if a user with that email exists
     */
    boolean existsByEmailIgnoreCase(String email);
}
