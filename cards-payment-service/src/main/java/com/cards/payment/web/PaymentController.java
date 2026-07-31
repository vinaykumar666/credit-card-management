package com.cards.payment.web;

import com.cards.payment.dto.InitiatePaymentRequest;
import com.cards.payment.dto.PaymentResponse;
import com.cards.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for payment APIs under {@code /api/v1/payments}.
 * Exposes endpoints to start a payment and fetch one by id.
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Creates the controller with the payment application service.
     *
     * @param paymentService service that handles payment business logic
     */
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Starts a new payment from the request body.
     *
     * @param request validated payment initiation details
     * @return HTTP 201 with the resulting payment state
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> initiate(@Valid @RequestBody InitiatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.initiate(request));
    }

    /**
     * Returns a payment by its id.
     *
     * @param id payment identifier
     * @return the payment details
     */
    @GetMapping("/{id}")
    public PaymentResponse getById(@PathVariable UUID id) {
        return paymentService.getById(id);
    }
}
