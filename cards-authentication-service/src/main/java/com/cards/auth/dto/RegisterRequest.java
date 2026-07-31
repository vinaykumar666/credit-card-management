package com.cards.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for registering a new user account.
 */
@Data
public class RegisterRequest {

    /** Unique email address for the new account. */
    @NotBlank
    @Email
    private String email;

    /** Password for the new account (8–100 characters). */
    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    /** Display name of the user. */
    @NotBlank
    @Size(max = 255)
    private String fullName;
}
