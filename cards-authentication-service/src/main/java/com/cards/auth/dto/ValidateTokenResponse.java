package com.cards.auth.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

/**
 * Response returned after checking an access token.
 * When {@code valid} is false, identity fields are typically unset.
 */
@Value
@Builder
public class ValidateTokenResponse {

    /** Whether the token was successfully decoded and validated. */
    boolean valid;

    /** Subject user id from the JWT when valid; otherwise null. */
    UUID userId;

    /** Email claim from the JWT when valid; otherwise null. */
    String email;

    /** Role names from the JWT when valid; otherwise null. */
    List<String> roles;
}
