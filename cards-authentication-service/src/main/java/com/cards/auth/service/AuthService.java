package com.cards.auth.service;

import com.cards.auth.domain.RefreshToken;
import com.cards.auth.domain.Role;
import com.cards.auth.domain.User;
import com.cards.auth.dto.LoginRequest;
import com.cards.auth.dto.RefreshRequest;
import com.cards.auth.dto.RegisterRequest;
import com.cards.auth.dto.TokenResponse;
import com.cards.auth.dto.ValidateTokenRequest;
import com.cards.auth.dto.ValidateTokenResponse;
import com.cards.auth.repository.RefreshTokenRepository;
import com.cards.auth.repository.RoleRepository;
import com.cards.auth.repository.UserRepository;
import com.cards.auth.security.OAuth2ClientProperties;
import com.cards.auth.security.OAuth2TokenIssuer;
import com.cards.common.error.ConflictException;
import com.cards.common.error.ErrorCodes;
import com.cards.common.error.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Core authentication business logic for register, login, refresh, and token validation.
 * Creates users, verifies passwords, issues JWT access tokens, and stores hashed refresh tokens.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2TokenIssuer tokenIssuer;
    private final OAuth2ClientProperties oauth2ClientProperties;
    private final JwtDecoder jwtDecoder;

    /**
     * Creates a new user with {@code ROLE_USER} and returns issued tokens.
     * Fails if the email is already registered or the default role is missing.
     *
     * @param request registration details
     * @return access and refresh tokens for the new user
     * @throws ConflictException if the email is already in use
     */
    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictException(ErrorCodes.AUTH_002);
        }
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException(ErrorCodes.AUTH_007));

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .enabled(true)
                .roles(Set.of(userRole))
                .build();
        userRepository.save(user);
        return issueTokens(user);
    }

    /**
     * Authenticates a user by email and password and returns issued tokens.
     *
     * @param request login credentials
     * @return access and refresh tokens for the authenticated user
     * @throws UnauthorizedException if the user is missing, disabled, or the password is wrong
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(ErrorCodes.AUTH_001));
        if (!user.isEnabled() || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException(ErrorCodes.AUTH_001);
        }
        return issueTokens(user);
    }

    /**
     * Revokes the presented refresh token (if valid) and issues a new token pair.
     * Expired or unknown refresh tokens are rejected.
     *
     * @param request body containing the refresh token
     * @return newly issued access and refresh tokens
     * @throws UnauthorizedException if the refresh token is invalid, revoked, or expired
     */
    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        String hash = hashToken(request.getRefreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new UnauthorizedException(ErrorCodes.AUTH_003));
        if (stored.isExpired()) {
            stored.setRevoked(true);
            throw new UnauthorizedException(ErrorCodes.AUTH_003);
        }
        stored.setRevoked(true);
        return issueTokens(stored.getUser());
    }

    /**
     * Decodes and validates a JWT access token, returning identity claims when successful.
     * Invalid or expired tokens produce a response with {@code valid=false} instead of throwing.
     *
     * @param request body containing the JWT to check
     * @return validation result; includes user id, email, and roles when the token is valid
     */
    public ValidateTokenResponse validate(ValidateTokenRequest request) {
        try {
            Jwt jwt = jwtDecoder.decode(request.getToken());
            @SuppressWarnings("unchecked")
            List<String> roles = jwt.getClaim("roles");
            return ValidateTokenResponse.builder()
                    .valid(true)
                    .userId(UUID.fromString(jwt.getSubject()))
                    .email(jwt.getClaimAsString("email"))
                    .roles(roles)
                    .build();
        } catch (Exception ex) {
            return ValidateTokenResponse.builder().valid(false).build();
        }
    }

    /**
     * Issues a JWT access token and a new opaque refresh token for the given user.
     * Persists only a SHA-256 hash of the refresh token.
     *
     * @param user the authenticated user receiving tokens
     * @return token response with access token, refresh token, and user identity
     */
    private TokenResponse issueTokens(User user) {
        String accessToken = tokenIssuer.issueAccessToken(user);
        String refreshToken = UUID.randomUUID() + "." + UUID.randomUUID();
        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(refreshToken))
                .expiresAt(Instant.now().plusSeconds(oauth2ClientProperties.getRefreshTokenTtlDays() * 24 * 3600))
                .revoked(false)
                .build();
        refreshTokenRepository.save(entity);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInSeconds(oauth2ClientProperties.getAccessTokenTtlMinutes() * 60)
                .userId(user.getId())
                .email(user.getEmail())
                .build();
    }

    /**
     * Returns the SHA-256 hex digest of a token string for safe storage lookup.
     *
     * @param token the raw refresh token value
     * @return hex-encoded SHA-256 hash
     * @throws IllegalStateException if SHA-256 is not available on the JVM
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
