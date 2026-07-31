package com.cards.common.logging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Emits structured {@code START}/{@code END} lifecycle logs around a service method.
 * Prefer pairing with {@link LifecycleLog} context fields (userId, amount, etc.).
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MethodLifecycle {

    /** Logical method name shown in logs (defaults to Java method name + {@code ()}). */
    String value() default "";
}
