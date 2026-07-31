package com.cards.payment.strategy;

import com.cards.common.error.ErrorCodes;
import com.cards.common.error.ValidationBusinessException;
import com.cards.payment.domain.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Looks up the {@link PaymentStrategy} for a given {@link PaymentMethod}.
 * Builds a map from all Spring-registered strategy beans at startup.
 */
@Component
public class PaymentStrategyFactory {

    private final Map<PaymentMethod, PaymentStrategy> strategies;

    /**
     * Registers each strategy under the payment method it supports.
     *
     * @param strategyList all {@link PaymentStrategy} beans in the application context
     */
    public PaymentStrategyFactory(List<PaymentStrategy> strategyList) {
        this.strategies = new EnumMap<>(PaymentMethod.class);
        for (PaymentStrategy strategy : strategyList) {
            strategies.put(strategy.supports(), strategy);
        }
    }

    /**
     * Returns the strategy for the given payment method.
     *
     * @param method the payment method to resolve
     * @return the matching {@link PaymentStrategy}
     * @throws ValidationBusinessException if no strategy is registered for the method
     */
    public PaymentStrategy getStrategy(PaymentMethod method) {
        PaymentStrategy strategy = strategies.get(method);
        if (strategy == null) {
            throw new ValidationBusinessException(ErrorCodes.PAY_002, "Unsupported payment method: " + method);
        }
        return strategy;
    }
}
