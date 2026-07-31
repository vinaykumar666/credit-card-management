package com.cards.payment.strategy;

import com.cards.common.error.ErrorCodes;
import com.cards.common.error.ValidationBusinessException;
import com.cards.payment.domain.Payment;
import com.cards.payment.domain.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentStrategyFactoryTest {

    @Test
    void returnsRegisteredStrategy() {
        PaymentStrategy cardStrategy = new StubStrategy(PaymentMethod.CARD);
        PaymentStrategyFactory factory = new PaymentStrategyFactory(List.of(cardStrategy));

        assertSame(cardStrategy, factory.getStrategy(PaymentMethod.CARD));
    }

    @Test
    void throwsValidationExceptionForUnsupportedMethod() {
        PaymentStrategyFactory factory = new PaymentStrategyFactory(List.of(new StubStrategy(PaymentMethod.CARD)));

        ValidationBusinessException ex = assertThrows(
                ValidationBusinessException.class,
                () -> factory.getStrategy(PaymentMethod.UPI)
        );
        assertEquals(ErrorCodes.PAY_002, ex.getErrorCode());
    }

    private static final class StubStrategy implements PaymentStrategy {
        private final PaymentMethod method;

        private StubStrategy(PaymentMethod method) {
            this.method = method;
        }

        @Override
        public PaymentMethod supports() {
            return method;
        }

        @Override
        public PaymentResult execute(Payment payment) {
            return PaymentResult.success("stub-ref");
        }
    }
}
