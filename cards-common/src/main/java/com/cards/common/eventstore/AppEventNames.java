package com.cards.common.eventstore;

/**
 * Canonical footfall / lifecycle event names persisted in {@code app_event}.
 */
public final class AppEventNames {

    public static final String HTTP_FOOTFALL = "HTTP_FOOTFALL";
    public static final String METHOD_START = "METHOD_START";
    public static final String METHOD_END = "METHOD_END";
    public static final String METHOD_ERROR = "METHOD_ERROR";
    public static final String KAFKA_CONSUMED = "KAFKA_CONSUMED";
    public static final String KAFKA_DLT = "KAFKA_DLT";
    public static final String LOGIN = "LOGIN";
    public static final String PAYMENT = "PAYMENT";
    public static final String TRANSFER = "TRANSFER";
    public static final String BILL_PAY = "BILL_PAY";

    private AppEventNames() {
    }
}
