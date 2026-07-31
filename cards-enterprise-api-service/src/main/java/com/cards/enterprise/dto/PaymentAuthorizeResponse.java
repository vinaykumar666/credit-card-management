package com.cards.enterprise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Platform domain response for enterprise payment authorization.
 * Summarizes whether the external network approved the payment and why.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAuthorizeResponse {

    /** Platform payment identifier that was authorized. */
    private String paymentId;
    /** {@code true} when the external network approved the payment. */
    private boolean approved;
    /** External network reference for the authorization attempt. */
    private String externalReference;
    /** Result message from the external network. */
    private String message;
}
