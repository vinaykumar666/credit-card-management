package com.cards.payment.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body from the enterprise payment authorize API.
 * Indicates approval and optional external reference or message.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterprisePaymentResponse {

    /** Payment id that was authorized. */
    private String paymentId;
    /** {@code true} when the enterprise API approved the payment. */
    private boolean approved;
    /** External reference from the enterprise network when approved. */
    private String externalReference;
    /** Status or error message from the enterprise API. */
    private String message;
}
