package com.cards.payment.service;

import com.cards.common.correlation.CorrelationConstants;
import com.cards.common.error.ErrorCodes;
import com.cards.common.error.NotFoundException;
import com.cards.common.event.NotificationRequestedEvent;
import com.cards.common.event.PaymentCompletedEvent;
import com.cards.common.event.PaymentFailedEvent;
import com.cards.payment.domain.LedgerEntry;
import com.cards.payment.domain.Payment;
import com.cards.payment.domain.PaymentStatus;
import com.cards.payment.dto.InitiatePaymentRequest;
import com.cards.payment.dto.PaymentResponse;
import com.cards.payment.kafka.KafkaPaymentEventPublisher;
import com.cards.payment.repository.LedgerEntryRepository;
import com.cards.payment.repository.PaymentRepository;
import com.cards.payment.strategy.PaymentResult;
import com.cards.payment.strategy.PaymentStrategy;
import com.cards.payment.strategy.PaymentStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Application service for creating and looking up payments.
 * Saves a pending payment, runs the matching strategy, then completes or fails it
 * with ledger updates and Kafka events.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final PaymentStrategyFactory strategyFactory;
    private final KafkaPaymentEventPublisher eventPublisher;

    /**
     * Creates the payment service with its persistence, strategy, and event dependencies.
     *
     * @param paymentRepository     repository for payment entities
     * @param ledgerEntryRepository repository for ledger entries
     * @param strategyFactory       factory that resolves the payment strategy
     * @param eventPublisher        publisher for payment and notification Kafka events
     */
    public PaymentService(
            PaymentRepository paymentRepository,
            LedgerEntryRepository ledgerEntryRepository,
            PaymentStrategyFactory strategyFactory,
            KafkaPaymentEventPublisher eventPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.strategyFactory = strategyFactory;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a pending payment, runs the strategy for its method, and returns the final state.
     * On success, writes a ledger debit and publishes completed and notification events.
     * On failure, publishes failed and notification events.
     *
     * @param request details of the payment to start
     * @return the payment after processing (completed or failed)
     */
    @Transactional
    public PaymentResponse initiate(InitiatePaymentRequest request) {
        String correlationId = MDC.get(CorrelationConstants.MDC_CORRELATION_ID);

        Payment payment = Payment.builder()
                .accountId(request.getAccountId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency().toUpperCase())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .correlationId(correlationId)
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment created id={} method={} status=PENDING", payment.getId(), payment.getPaymentMethod());

        PaymentStrategy strategy = strategyFactory.getStrategy(payment.getPaymentMethod());
        PaymentResult result = strategy.execute(payment);

        if (result.isSuccess()) {
            return completePayment(payment, result.getExternalRef());
        }
        return failPayment(payment, result.getFailureReason());
    }

    /**
     * Loads a payment by id and maps it to an API response.
     *
     * @param id payment identifier
     * @return the payment as a {@link PaymentResponse}
     * @throws NotFoundException if no payment exists for the id
     */
    @Transactional(readOnly = true)
    public PaymentResponse getById(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.PAY_001));
        return toResponse(payment);
    }

    /**
     * Marks the payment completed, saves a debit ledger entry, and publishes related events.
     *
     * @param payment     payment to complete
     * @param externalRef reference from the payment processor
     * @return API response for the completed payment
     */
    private PaymentResponse completePayment(Payment payment, String externalRef) {
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setExternalRef(externalRef);
        payment.setFailureReason(null);
        payment = paymentRepository.save(payment);

        ledgerEntryRepository.save(LedgerEntry.builder()
                .paymentId(payment.getId())
                .accountId(payment.getAccountId())
                .entryType("DEBIT")
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .build());

        Instant completedAt = Instant.now();
        eventPublisher.publishCompleted(PaymentCompletedEvent.builder()
                .paymentId(payment.getId())
                .accountId(payment.getAccountId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod().name())
                .correlationId(payment.getCorrelationId())
                .completedAt(completedAt)
                .build());

        eventPublisher.publishNotification(NotificationRequestedEvent.builder()
                .notificationId(UUID.randomUUID())
                .userId(payment.getUserId())
                .channel("EMAIL")
                .template("PAYMENT_COMPLETED")
                .recipient(payment.getUserId().toString())
                .placeholders(Map.of(
                        "paymentId", payment.getId().toString(),
                        "amount", payment.getAmount().toPlainString(),
                        "currency", payment.getCurrency(),
                        "status", PaymentStatus.COMPLETED.name()
                ))
                .correlationId(payment.getCorrelationId())
                .requestedAt(completedAt)
                .build());

        log.info("Payment completed id={} externalRef={}", payment.getId(), externalRef);
        return toResponse(payment);
    }

    /**
     * Marks the payment failed, stores the reason, and publishes related events.
     *
     * @param payment payment to fail
     * @param reason  why the payment failed
     * @return API response for the failed payment
     */
    private PaymentResponse failPayment(Payment payment, String reason) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        payment = paymentRepository.save(payment);

        Instant failedAt = Instant.now();
        eventPublisher.publishFailed(PaymentFailedEvent.builder()
                .paymentId(payment.getId())
                .accountId(payment.getAccountId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod().name())
                .reason(reason)
                .correlationId(payment.getCorrelationId())
                .failedAt(failedAt)
                .build());

        eventPublisher.publishNotification(NotificationRequestedEvent.builder()
                .notificationId(UUID.randomUUID())
                .userId(payment.getUserId())
                .channel("EMAIL")
                .template("PAYMENT_FAILED")
                .recipient(payment.getUserId().toString())
                .placeholders(Map.of(
                        "paymentId", payment.getId().toString(),
                        "amount", payment.getAmount().toPlainString(),
                        "currency", payment.getCurrency(),
                        "reason", reason != null ? reason : "Unknown"
                ))
                .correlationId(payment.getCorrelationId())
                .requestedAt(failedAt)
                .build());

        log.info("Payment failed id={} reason={}", payment.getId(), reason);
        return toResponse(payment);
    }

    /**
     * Maps a {@link Payment} entity to a {@link PaymentResponse}.
     *
     * @param payment domain payment
     * @return API response DTO
     */
    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .accountId(payment.getAccountId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .externalRef(payment.getExternalRef())
                .failureReason(payment.getFailureReason())
                .correlationId(payment.getCorrelationId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
