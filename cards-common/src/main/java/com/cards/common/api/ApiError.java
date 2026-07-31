package com.cards.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Simple mutable API error DTO used where a Lombok bean is preferred over {@code ErrorResponse}.
 * Holds timestamp, HTTP status, short error label, message, path, and correlation id.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

    /** When the error was created. */
    private Instant timestamp;
    /** HTTP status code. */
    private int status;
    /** Short error label (for example reason phrase). */
    private String error;
    /** Human-readable error message. */
    private String message;
    /** Request path that failed. */
    private String path;
    /** Request correlation id, if available. */
    private String correlationId;
}
