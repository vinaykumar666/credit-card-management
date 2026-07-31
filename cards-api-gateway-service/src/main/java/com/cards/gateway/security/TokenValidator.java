package com.cards.gateway.security;

/**
 * Abstraction for checking Authorization headers at the gateway (dependency inversion).
 * Concrete JWT libraries can replace {@link JwtTokenValidator} without changing gateway filters.
 */
public interface TokenValidator {

    /**
     * Checks whether the raw Authorization header is acceptable for gateway pass-through.
     *
     * @param authorizationHeader raw Authorization header value (may be {@code null})
     * @return {@code true} if the token is acceptable for gateway pass-through
     */
    boolean isValid(String authorizationHeader);
}
