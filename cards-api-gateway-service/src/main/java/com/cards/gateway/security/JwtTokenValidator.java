package com.cards.gateway.security;

import org.springframework.stereotype.Component;

/**
 * Stub JWT validator that only checks for a non-empty Bearer token.
 * Full JWT signature and claims validation remains in the authentication service; this bean can later be swapped for a library-backed implementation.
 */
@Component
public class JwtTokenValidator implements TokenValidator {

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Returns {@code true} when the header has a non-blank token after a case-insensitive {@code Bearer } prefix.
     *
     * @param authorizationHeader raw Authorization header value (may be {@code null})
     * @return {@code true} if a Bearer token value is present; otherwise {@code false}
     */
    @Override
    public boolean isValid(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return false;
        }
        if (!authorizationHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return false;
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        return !token.isEmpty();
    }
}
