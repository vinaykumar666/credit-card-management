package com.cards.auth.repository;

import com.cards.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for {@link RefreshToken} entities.
 * Used to look up active (non-revoked) refresh tokens by their SHA-256 hash.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Finds a non-revoked refresh token matching the given hash.
     *
     * @param tokenHash SHA-256 hex hash of the raw refresh token
     * @return the matching token if present and not revoked
     */
    Optional<RefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);
}
