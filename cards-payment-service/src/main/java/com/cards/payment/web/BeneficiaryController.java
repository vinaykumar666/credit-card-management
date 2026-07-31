package com.cards.payment.web;

import com.cards.payment.dto.BeneficiaryRequest;
import com.cards.payment.dto.BeneficiaryResponse;
import com.cards.payment.service.BeneficiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST APIs for managing beneficiaries (saved payees).
 */
@RestController
@RequestMapping("/api/v1/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @GetMapping
    public List<BeneficiaryResponse> list(
            @RequestParam UUID userId,
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        return activeOnly
                ? beneficiaryService.listActiveByUser(userId)
                : beneficiaryService.listByUser(userId);
    }

    @GetMapping("/{id}")
    public BeneficiaryResponse get(@PathVariable UUID id, @RequestParam UUID userId) {
        return beneficiaryService.getForUser(id, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BeneficiaryResponse create(@Valid @RequestBody BeneficiaryRequest request) {
        return beneficiaryService.create(request);
    }

    @PutMapping("/{id}")
    public BeneficiaryResponse update(@PathVariable UUID id, @Valid @RequestBody BeneficiaryRequest request) {
        return beneficiaryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public BeneficiaryResponse deactivate(@PathVariable UUID id, @RequestParam UUID userId) {
        return beneficiaryService.deactivate(id, userId);
    }
}
