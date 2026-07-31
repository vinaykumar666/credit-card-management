package com.cards.enterprise.web;

import com.cards.enterprise.adapter.EnterprisePaymentAdapter;
import com.cards.enterprise.dto.PaymentAuthorizeRequest;
import com.cards.enterprise.dto.PaymentAuthorizeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for enterprise payment authorization and adapter health checks.
 * Exposes {@code /api/v1/enterprise} endpoints that delegate to {@link EnterprisePaymentAdapter}.
 */
@RestController
@RequestMapping("/api/v1/enterprise")
@RequiredArgsConstructor
@Tag(name = "Enterprise Payments", description = "Adapter to external payment / core-banking network")
public class EnterprisePaymentController {

    private final EnterprisePaymentAdapter enterprisePaymentAdapter;

    /**
     * Authorizes a payment through the external network adapter.
     *
     * @param request validated platform authorization request
     * @return HTTP 200 with the authorization result
     */
    @PostMapping("/payments/authorize")
    @Operation(summary = "Authorize a payment via the external network adapter")
    public ResponseEntity<PaymentAuthorizeResponse> authorize(@Valid @RequestBody PaymentAuthorizeRequest request) {
        return ResponseEntity.ok(enterprisePaymentAdapter.authorize(request));
    }

    /**
     * Simple health check for the enterprise adapter service.
     *
     * @return HTTP 200 with status UP and the service name
     */
    @GetMapping("/health-check")
    @Operation(summary = "Enterprise adapter health check")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "cards-enterprise-api-service"
        ));
    }
}
