package com.cards.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for exchanging a refresh token for a new token pair.
 */
@Data
public class RefreshRequest {

    /** Opaque refresh token previously issued by this service. */
    @NotBlank
    private String refreshToken;
}
