package com.cards.enterprise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Proprietary response returned by the simulated external / core-banking network.
 * Carries result code, external reference, and a human-readable message.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalPaymentResponse {

    /** Network result code (for example, {@code 00} approved or {@code 05} declined). */
    private String resultCode;
    /** External reference assigned by the network. */
    private String externalRef;
    /** Short result message from the network. */
    private String resultMessage;
}
