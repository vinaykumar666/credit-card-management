package com.cards.common.error;

/**
 * String constants for catalog error codes used across services.
 * HTTP status and default messages live in {@code error-codes.yml}, not in this class.
 */
public final class ErrorCodes {

    /** Invalid credentials. */
    public static final String AUTH_001 = "AUTH_001";
    /** Email already registered. */
    public static final String AUTH_002 = "AUTH_002";
    /** Invalid or expired refresh token. */
    public static final String AUTH_003 = "AUTH_003";
    /** Unauthorized. */
    public static final String AUTH_004 = "AUTH_004";
    /** Invalid OAuth2 client request. */
    public static final String AUTH_005 = "AUTH_005";
    /** User not found. */
    public static final String AUTH_006 = "AUTH_006";
    /** Default role configuration missing. */
    public static final String AUTH_007 = "AUTH_007";
    /** Account not found. */
    public static final String ACCT_001 = "ACCT_001";
    /** Invalid account request. */
    public static final String ACCT_002 = "ACCT_002";
    /** Account number already exists. */
    public static final String ACCT_003 = "ACCT_003";
    /** Payment not found. */
    public static final String PAY_001 = "PAY_001";
    /** Unsupported payment method. */
    public static final String PAY_002 = "PAY_002";
    /** Payment processing failed. */
    public static final String PAY_003 = "PAY_003";
    /** Enterprise payment network unavailable. */
    public static final String PAY_004 = "PAY_004";
    /** Notification not found. */
    public static final String NOTIF_001 = "NOTIF_001";
    /** Unsupported notification channel. */
    public static final String NOTIF_002 = "NOTIF_002";
    /** External network authorization declined. */
    public static final String ENT_001 = "ENT_001";
    /** Invalid enterprise payment request. */
    public static final String ENT_002 = "ENT_002";
    /** Missing required header X-Channel-Id. */
    public static final String BFF_001 = "BFF_001";
    /** Missing required header X-Client-Id. */
    public static final String BFF_002 = "BFF_002";
    /** Channel is not allowed. */
    public static final String BFF_003 = "BFF_003";
    /** Client is not allowed. */
    public static final String BFF_004 = "BFF_004";
    /** Downstream aggregation failed. */
    public static final String BFF_005 = "BFF_005";
    /** Downstream service unavailable. */
    public static final String GW_001 = "GW_001";
    /** Missing or invalid Authorization header. */
    public static final String GW_002 = "GW_002";
    /** Missing required tenant headers. */
    public static final String GW_003 = "GW_003";
    /** Validation failed. */
    public static final String COMMON_001 = "COMMON_001";
    /** Unexpected internal error. */
    public static final String COMMON_002 = "COMMON_002";
    /** Missing correlation context. */
    public static final String COMMON_003 = "COMMON_003";

    private ErrorCodes() {
    }
}
