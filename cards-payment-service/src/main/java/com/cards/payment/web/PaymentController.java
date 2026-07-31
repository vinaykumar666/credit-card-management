package com.cards.payment.web;

import com.cards.common.logging.LifecycleLog;
import com.cards.payment.dto.InitiatePaymentRequest;
import com.cards.payment.dto.MakePaymentRequest;
import com.cards.payment.dto.PaymentResponse;
import com.cards.payment.dto.TransferMoneyRequest;
import com.cards.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Banking payment APIs: card settlement, transfer to beneficiary, bill pay, and history.
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** Self / card settlement (no beneficiary). */
    @PostMapping
    public ResponseEntity<PaymentResponse> initiate(@Valid @RequestBody InitiatePaymentRequest request) {
        LifecycleLog.bind(request.getUserId(), null, request.getAmount(), null);
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiate(request));
        } finally {
            LifecycleLog.clearBusinessContext();
        }
    }

    /** Transfer money to a saved beneficiary. */
    @PostMapping("/transfer")
    public ResponseEntity<PaymentResponse> transfer(@Valid @RequestBody TransferMoneyRequest request) {
        LifecycleLog.bind(request.getUserId(), null, request.getAmount(), null);
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.transfer(request));
        } finally {
            LifecycleLog.clearBusinessContext();
        }
    }

    /** Make a bill / merchant payment. */
    @PostMapping("/bill-pay")
    public ResponseEntity<PaymentResponse> makePayment(@Valid @RequestBody MakePaymentRequest request) {
        LifecycleLog.bind(request.getUserId(), null, request.getAmount(), null);
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.makePayment(request));
        } finally {
            LifecycleLog.clearBusinessContext();
        }
    }

    @GetMapping("/{id}")
    public PaymentResponse getById(@PathVariable UUID id) {
        return paymentService.getById(id);
    }

    @GetMapping
    public List<PaymentResponse> history(@RequestParam UUID userId) {
        return paymentService.listByUser(userId);
    }
}
