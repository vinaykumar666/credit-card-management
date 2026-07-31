package com.cards.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for validating a JWT access token.
 */
@Data
public class ValidateTokenRequest {

    /** Compact JWT access token to validate. */
    @NotBlank
    private String token;
}
