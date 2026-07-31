package com.cards.enterprise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Proprietary wire format expected by the simulated external / core-banking network.
 * Field names match the external API style rather than the platform domain model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalPaymentRequest {

    /** External transaction identifier (mapped from platform payment id). */
    private String txnId;
    /** Tokenized card PAN used by the external network. */
    private String cardPanToken;
    /** Payment amount. */
    private BigDecimal amt;
    /** Currency code expected by the external network. */
    private String ccy;
    /** Merchant code expected by the external network. */
    private String merchantCode;
}
