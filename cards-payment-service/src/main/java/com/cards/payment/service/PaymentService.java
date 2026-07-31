package com.cards.payment.service;

import com.cards.common.correlation.CorrelationConstants;
import com.cards.common.error.ErrorCodes;
import com.cards.common.error.NotFoundException;
import com.cards.common.error.ValidationBusinessException;
import com.cards.common.event.NotificationRequestedEvent;
import com.cards.common.event.PaymentCompletedEvent;
import com.cards.common.event.PaymentFailedEvent;
import com.cards.common.logging.LifecycleLog;
import com.cards.common.logging.MethodLifecycle;
import com.cards.payment.domain.Beneficiary;
import com.cards.payment.domain.LedgerEntry;
import com.cards.payment.domain.Payment;
import com.cards.payment.domain.PaymentMethod;
import com.cards.payment.domain.PaymentStatus;
import com.cards.payment.domain.PaymentType;
import com.cards.payment.dto.InitiatePaymentRequest;
import com.cards.payment.dto.MakePaymentRequest;
import com.cards.payment.dto.PaymentResponse;
import com.cards.payment.dto.TransferMoneyRequest;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates card payments, transfers to beneficiaries, and bill payments.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final PaymentStrategyFactory strategyFactory;
    private final KafkaPaymentEventPublisher eventPublisher;
    private final BeneficiaryService beneficiaryService;

    public PaymentService(
            PaymentRepository paymentRepository,
            LedgerEntryRepository ledgerEntryRepository,
            PaymentStrategyFactory strategyFactory,
            KafkaPaymentEventPublisher eventPublisher,
            BeneficiaryService beneficiaryService
    ) {
        this.paymentRepository = paymentRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.strategyFactory = strategyFactory;
        this.eventPublisher = eventPublisher;
        this.beneficiaryService = beneficiaryService;
    }

    /**
     * Self / card payment (legacy + simple settlement without a beneficiary).
     */
    @MethodLifecycle("initiate")
    @Transactional
    public PaymentResponse initiate(InitiatePaymentRequest request) {
        Payment payment = basePayment(
                request.getAccountId(),
                request.getUserId(),
                request.getAmount(),
                request.getCurrency(),
                request.getPaymentMethod(),
                PaymentType.CARD_PAYMENT,
                null,
                null,
                null,
                null,
                null,
                request.getRemarks(),
                null
        );
        return process(payment);
    }

    /**
     * Transfers money to a saved beneficiary (P2P / account transfer).
     */
    @MethodLifecycle("transfer")
    @Transactional
    public PaymentResponse transfer(TransferMoneyRequest request) {
        Beneficiary beneficiary = beneficiaryService.requireActiveOwned(
                request.getBeneficiaryId(), request.getUserId());
        Payment payment = basePayment(
                request.getAccountId(),
                request.getUserId(),
                request.getAmount(),
                request.getCurrency(),
                request.getPaymentMethod(),
                PaymentType.TRANSFER,
                beneficiary.getId(),
                beneficiary.getBeneficiaryName(),
                beneficiary.getAccountNumber(),
                beneficiary.getBankName(),
                beneficiary.getIfscOrRouting(),
                request.getRemarks(),
                "TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
        );
        return process(payment);
    }

    /**
     * Makes a bill / merchant payment using a saved merchant beneficiary or one-time payee details.
     */
    @MethodLifecycle("makePayment")
    @Transactional
    public PaymentResponse makePayment(MakePaymentRequest request) {
        UUID beneficiaryId = null;
        String name;
        String account;
        String bank;
        String ifsc;

        if (request.getBeneficiaryId() != null) {
            Beneficiary beneficiary = beneficiaryService.requireActiveOwned(
                    request.getBeneficiaryId(), request.getUserId());
            beneficiaryId = beneficiary.getId();
            name = beneficiary.getBeneficiaryName();
            account = beneficiary.getAccountNumber();
            bank = beneficiary.getBankName();
            ifsc = beneficiary.getIfscOrRouting();
        } else {
            if (isBlank(request.getPayeeName()) || isBlank(request.getPayeeAccountNumber())
                    || isBlank(request.getPayeeBankName()) || isBlank(request.getPayeeIfscOrRouting())) {
                throw new ValidationBusinessException(ErrorCodes.PAY_008,
                        "Provide beneficiaryId or full one-time payee details");
            }
            name = request.getPayeeName().trim();
            account = request.getPayeeAccountNumber().trim();
            bank = request.getPayeeBankName().trim();
            ifsc = request.getPayeeIfscOrRouting().trim().toUpperCase();
        }

        String reference = !isBlank(request.getBillReference())
                ? request.getBillReference().trim()
                : "BILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = basePayment(
                request.getAccountId(),
                request.getUserId(),
                request.getAmount(),
                request.getCurrency(),
                request.getPaymentMethod(),
                PaymentType.BILL_PAYMENT,
                beneficiaryId,
                name,
                account,
                bank,
                ifsc,
                request.getRemarks(),
                reference
        );
        return process(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getById(UUID id) {
        return toResponse(paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.PAY_001)));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listByUser(UUID userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private PaymentResponse process(Payment payment) {
        payment = paymentRepository.save(payment);
        LifecycleLog.bindTransactionId(payment.getId());
        log.info("Payment created id={} type={} status=PENDING", payment.getId(), payment.getPaymentType());

        PaymentStrategy strategy = strategyFactory.getStrategy(payment.getPaymentMethod());
        PaymentResult result = strategy.execute(payment);
        if (result.isSuccess()) {
            return completePayment(payment, result.getExternalRef());
        }
        return failPayment(payment, result.getFailureReason());
    }

    private Payment basePayment(
            UUID accountId,
            UUID userId,
            java.math.BigDecimal amount,
            String currency,
            PaymentMethod method,
            PaymentType type,
            UUID beneficiaryId,
            String beneficiaryName,
            String beneficiaryAccount,
            String bankName,
            String ifsc,
            String remarks,
            String referenceNumber
    ) {
        return Payment.builder()
                .accountId(accountId)
                .userId(userId)
                .amount(amount)
                .currency(currency.toUpperCase())
                .paymentMethod(method)
                .paymentType(type)
                .status(PaymentStatus.PENDING)
                .correlationId(MDC.get(CorrelationConstants.MDC_CORRELATION_ID))
                .beneficiaryId(beneficiaryId)
                .beneficiaryName(beneficiaryName)
                .beneficiaryAccount(beneficiaryAccount)
                .bankName(bankName)
                .ifscOrRouting(ifsc)
                .remarks(remarks)
                .referenceNumber(referenceNumber)
                .build();
    }

    private PaymentResponse completePayment(Payment payment, String externalRef) {
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setExternalRef(externalRef);
        payment.setFailureReason(null);
        if (isBlank(payment.getReferenceNumber())) {
            payment.setReferenceNumber("PAY-" + payment.getId().toString().substring(0, 8).toUpperCase());
        }
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
                .template(payment.getPaymentType() == PaymentType.TRANSFER ? "TRANSFER_COMPLETED" : "PAYMENT_COMPLETED")
                .recipient(payment.getUserId().toString())
                .placeholders(Map.of(
                        "paymentId", payment.getId().toString(),
                        "amount", payment.getAmount().toPlainString(),
                        "currency", payment.getCurrency(),
                        "status", PaymentStatus.COMPLETED.name(),
                        "beneficiary", payment.getBeneficiaryName() != null ? payment.getBeneficiaryName() : "self"
                ))
                .correlationId(payment.getCorrelationId())
                .requestedAt(completedAt)
                .build());

        log.info("Payment completed id={} type={} ref={}", payment.getId(), payment.getPaymentType(), externalRef);
        return toResponse(payment);
    }

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

        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .accountId(payment.getAccountId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .paymentType(payment.getPaymentType())
                .status(payment.getStatus())
                .beneficiaryId(payment.getBeneficiaryId())
                .beneficiaryName(payment.getBeneficiaryName())
                .beneficiaryAccount(payment.getBeneficiaryAccount())
                .bankName(payment.getBankName())
                .ifscOrRouting(payment.getIfscOrRouting())
                .remarks(payment.getRemarks())
                .referenceNumber(payment.getReferenceNumber())
                .externalRef(payment.getExternalRef())
                .failureReason(payment.getFailureReason())
                .correlationId(payment.getCorrelationId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
