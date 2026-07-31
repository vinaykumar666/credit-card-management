package com.cards.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Structured START/END method lifecycle logging used across microservices.
 *
 * <p>Example output:
 * <pre>
 * event="START", method="makePayment()", userId=..., userName=..., amount=..., transactionId=...
 * event="END", method="makePayment()", userId=..., userName=..., amount=..., transactionTime=12ms
 * </pre>
 */
public final class LifecycleLog {

    public static final String MDC_USER_ID = "userId";
    public static final String MDC_USER_NAME = "userName";
    public static final String MDC_AMOUNT = "amount";
    public static final String MDC_TRANSACTION_ID = "transactionId";
    public static final String MDC_METHOD = "method";
    public static final String MDC_EVENT = "event";

    private static final Logger log = LoggerFactory.getLogger("com.cards.lifecycle");

    private LifecycleLog() {
    }

    public static void start(String method, Map<String, Object> fields) {
        Map<String, Object> payload = base(method, "START", fields);
        putMdc(payload);
        log.info("{}", format(payload));
    }

    public static void end(String method, Map<String, Object> fields, long durationMs) {
        Map<String, Object> payload = base(method, "END", fields);
        payload.put("transactionTime", durationMs + "ms");
        putMdc(payload);
        log.info("{}", format(payload));
        clearTransientMdc();
    }

    public static void fail(String method, Map<String, Object> fields, long durationMs, Throwable error) {
        Map<String, Object> payload = base(method, "ERROR", fields);
        payload.put("transactionTime", durationMs + "ms");
        payload.put("error", error != null ? error.getClass().getSimpleName() : "Unknown");
        putMdc(payload);
        log.error("{}", format(payload));
        clearTransientMdc();
    }

    public static Map<String, Object> ctx(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (keyValues == null) {
            return map;
        }
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            Object key = keyValues[i];
            Object value = keyValues[i + 1];
            if (key != null) {
                map.put(String.valueOf(key), value);
            }
        }
        return map;
    }

    /**
     * Binds business context into MDC before a {@link MethodLifecycle}-advised call
     * so START logs include userId / amount / etc.
     */
    public static void bind(Object userId, String userName, Object amount, Object transactionId) {
        if (userId != null) {
            MDC.put(MDC_USER_ID, String.valueOf(userId));
        }
        if (userName != null && !userName.isBlank()) {
            MDC.put(MDC_USER_NAME, userName);
        }
        if (amount != null) {
            MDC.put(MDC_AMOUNT, String.valueOf(amount));
        }
        if (transactionId != null) {
            MDC.put(MDC_TRANSACTION_ID, String.valueOf(transactionId));
        }
    }

    public static void bindTransactionId(Object transactionId) {
        if (transactionId != null) {
            MDC.put(MDC_TRANSACTION_ID, String.valueOf(transactionId));
        }
    }

    public static void clearBusinessContext() {
        MDC.remove(MDC_USER_ID);
        MDC.remove(MDC_USER_NAME);
        clearTransientMdc();
    }

    private static Map<String, Object> base(String method, String event, Map<String, Object> fields) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", event);
        payload.put("method", normalizeMethod(method));
        if (fields != null) {
            fields.forEach((k, v) -> {
                if (k != null && v != null) {
                    payload.put(k, v);
                }
            });
        }
        return payload;
    }

    private static String normalizeMethod(String method) {
        if (method == null || method.isBlank()) {
            return "unknown()";
        }
        return method.endsWith("()") ? method : method + "()";
    }

    private static String format(Map<String, Object> payload) {
        return payload.entrySet().stream()
                .map(e -> e.getKey() + "=\"" + e.getValue() + "\"")
                .collect(Collectors.joining(", "));
    }

    private static void putMdc(Map<String, Object> payload) {
        payload.forEach((k, v) -> {
            if (v != null) {
                MDC.put(k, String.valueOf(v));
            }
        });
    }

    private static void clearTransientMdc() {
        MDC.remove(MDC_EVENT);
        MDC.remove(MDC_METHOD);
        MDC.remove(MDC_AMOUNT);
        MDC.remove(MDC_TRANSACTION_ID);
        MDC.remove("transactionTime");
        MDC.remove("error");
    }
}
