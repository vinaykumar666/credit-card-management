package com.cards.payment.client;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

/**
 * Request payload sent to the enterprise payment authorize API.
 */
@Value
@Builder
public class EnterprisePaymentRequest {

    /** Payment id in this service. */
    String paymentId;
    /** Token representing the card or account to charge. */
    String cardToken;
    /** Amount to authorize. */
    BigDecimal amount;
    /** ISO currency code. */
    String currency;
    /** Merchant identifier sent to the enterprise API. */
    String merchantId;
}
