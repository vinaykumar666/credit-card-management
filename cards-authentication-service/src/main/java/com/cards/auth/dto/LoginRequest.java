package com.cards.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for user login with email and password.
 */
@Data
public class LoginRequest {

    /** User email address. */
    @NotBlank
    @Email
    private String email;

    /** Plain-text password to verify against the stored hash. */
    @NotBlank
    private String password;
}
