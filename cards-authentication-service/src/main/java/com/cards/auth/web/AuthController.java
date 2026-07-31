package com.cards.auth.web;

import com.cards.auth.dto.LoginRequest;
import com.cards.auth.dto.RefreshRequest;
import com.cards.auth.dto.RegisterRequest;
import com.cards.auth.dto.TokenResponse;
import com.cards.auth.dto.ValidateTokenRequest;
import com.cards.auth.dto.ValidateTokenResponse;
import com.cards.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for first-party authentication flows.
 * Exposes register, login, refresh, and token validation under {@code /api/v1/auth}.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user and returns access and refresh tokens.
     *
     * @param request registration details (email, password, full name)
     * @return issued tokens and basic user identity
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * Authenticates an existing user and returns access and refresh tokens.
     *
     * @param request login credentials (email and password)
     * @return issued tokens and basic user identity
     */
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Exchanges a valid refresh token for a new access token and refresh token pair.
     *
     * @param request body containing the refresh token
     * @return newly issued tokens and basic user identity
     */
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    /**
     * Checks whether an access token is valid and returns identity claims when it is.
     *
     * @param request body containing the JWT access token to validate
     * @return validation result with user id, email, and roles when valid
     */
    @PostMapping("/validate")
    public ValidateTokenResponse validate(@Valid @RequestBody ValidateTokenRequest request) {
        return authService.validate(request);
    }
}
