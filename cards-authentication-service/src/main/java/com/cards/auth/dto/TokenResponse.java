package com.cards.auth.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Tokens and identity returned after register, login, or refresh.
 */
@Value
@Builder
public class TokenResponse {

    /** Signed JWT access token. */
    String accessToken;

    /** Opaque refresh token (stored hashed server-side). */
    String refreshToken;

    /** Token type, typically {@code Bearer}. */
    String tokenType;

    /** Access token lifetime in seconds. */
    long expiresInSeconds;

    /** Authenticated user's id. */
    UUID userId;

    /** Authenticated user's email. */
    String email;
}
