package com.cards.common.logging;

import com.cards.common.correlation.CorrelationConstants;
import com.cards.common.eventstore.AppEventNames;
import com.cards.common.eventstore.AppEventRecord;
import com.cards.common.eventstore.AppEventStore;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AOP advice that wraps {@link MethodLifecycle}-annotated methods with START/END structured logs
 * and optional {@code app_event} persistence.
 */
@Aspect
public class MethodLifecycleAspect {

    private final ObjectProvider<AppEventStore> appEventStore;
    private final String serviceName;

    public MethodLifecycleAspect(
            ObjectProvider<AppEventStore> appEventStore,
            @Value("${spring.application.name:unknown-service}") String serviceName
    ) {
        this.appEventStore = appEventStore;
        this.serviceName = serviceName;
    }

    @Around("@annotation(com.cards.common.logging.MethodLifecycle) || @within(com.cards.common.logging.MethodLifecycle)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        MethodLifecycle annotation = AnnotationUtils.findAnnotation(method, MethodLifecycle.class);
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(method.getDeclaringClass(), MethodLifecycle.class);
        }
        String methodName = annotation != null && !annotation.value().isBlank()
                ? annotation.value()
                : method.getName();

        Map<String, Object> fields = currentFields();

        long started = System.currentTimeMillis();
        LifecycleLog.start(methodName, fields);
        persist(AppEventNames.METHOD_START, "START", methodName, fields, null, null);
        try {
            Object result = pjp.proceed();
            long elapsed = System.currentTimeMillis() - started;
            // Re-read MDC so fields set inside the method (e.g. transactionId) appear on END.
            Map<String, Object> endFields = currentFields();
            LifecycleLog.end(methodName, endFields, elapsed);
            persist(AppEventNames.METHOD_END, "END", methodName, endFields, elapsed, "OK");
            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - started;
            Map<String, Object> errorFields = currentFields();
            LifecycleLog.fail(methodName, errorFields, elapsed, ex);
            persist(AppEventNames.METHOD_ERROR, "ERROR", methodName, errorFields, elapsed, ex.getClass().getSimpleName());
            throw ex;
        }
    }

    private static Map<String, Object> currentFields() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("userId", MDC.get(LifecycleLog.MDC_USER_ID));
        fields.put("userName", MDC.get(LifecycleLog.MDC_USER_NAME));
        fields.put("amount", MDC.get(LifecycleLog.MDC_AMOUNT));
        fields.put("transactionId", MDC.get(LifecycleLog.MDC_TRANSACTION_ID));
        return fields;
    }

    private void persist(
            String eventName,
            String phase,
            String methodName,
            Map<String, Object> fields,
            Long durationMs,
            String status
    ) {
        AppEventStore store = appEventStore.getIfAvailable();
        if (store == null) {
            return;
        }
        store.record(new AppEventRecord(
                eventName,
                phase,
                serviceName,
                methodName.endsWith("()") ? methodName : methodName + "()",
                parseUuid(stringVal(fields.get("userId"))),
                stringVal(fields.get("userName")),
                parseAmount(stringVal(fields.get("amount"))),
                stringVal(fields.get("transactionId")),
                MDC.get(CorrelationConstants.MDC_CORRELATION_ID),
                MDC.get(CorrelationConstants.MDC_CHANNEL_ID),
                MDC.get(CorrelationConstants.MDC_CLIENT_ID),
                null,
                null,
                status,
                durationMs,
                null
        ));
    }

    private static String stringVal(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static UUID parseUuid(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static BigDecimal parseAmount(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
