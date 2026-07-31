package com.cards.common.error;

/**
 * One entry from the shared error catalog: HTTP status plus default message.
 * Loaded from YAML and looked up by error code string.
 *
 * @param httpStatus HTTP status to return for this code
 * @param message    default client-facing message for this code
 */
public record ErrorCodeDefinition(int httpStatus, String message) {
}
