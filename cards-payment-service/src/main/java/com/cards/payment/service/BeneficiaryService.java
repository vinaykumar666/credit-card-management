package com.cards.payment.service;

import com.cards.common.error.ConflictException;
import com.cards.common.error.ErrorCodes;
import com.cards.common.error.NotFoundException;
import com.cards.common.error.ValidationBusinessException;
import com.cards.payment.domain.Beneficiary;
import com.cards.payment.domain.BeneficiaryStatus;
import com.cards.payment.dto.BeneficiaryRequest;
import com.cards.payment.dto.BeneficiaryResponse;
import com.cards.payment.repository.BeneficiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Manages saved beneficiaries (payees) for transfers and bill payments.
 */
@Service
@RequiredArgsConstructor
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;

    /**
     * Lists all beneficiaries for a user (active and inactive).
     */
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> listByUser(UUID userId) {
        return beneficiaryRepository.findByUserIdOrderByNicknameAsc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Lists only active beneficiaries suitable for new transfers/payments.
     */
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> listActiveByUser(UUID userId) {
        return beneficiaryRepository.findByUserIdAndStatusOrderByNicknameAsc(userId, BeneficiaryStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BeneficiaryResponse getForUser(UUID id, UUID userId) {
        return toResponse(requireOwned(id, userId));
    }

    /**
     * Adds a new beneficiary for the user.
     */
    @Transactional
    public BeneficiaryResponse create(BeneficiaryRequest request) {
        if (beneficiaryRepository.existsByUserIdAndAccountNumberIgnoreCase(
                request.getUserId(), request.getAccountNumber())) {
            throw new ConflictException(ErrorCodes.PAY_007);
        }
        Beneficiary saved = beneficiaryRepository.save(Beneficiary.builder()
                .userId(request.getUserId())
                .nickname(request.getNickname().trim())
                .beneficiaryName(request.getBeneficiaryName().trim())
                .accountNumber(request.getAccountNumber().trim())
                .bankName(request.getBankName().trim())
                .ifscOrRouting(request.getIfscOrRouting().trim().toUpperCase())
                .beneficiaryType(request.getBeneficiaryType())
                .status(BeneficiaryStatus.ACTIVE)
                .build());
        return toResponse(saved);
    }

    /**
     * Updates an existing beneficiary owned by the user.
     */
    @Transactional
    public BeneficiaryResponse update(UUID id, BeneficiaryRequest request) {
        Beneficiary existing = requireOwned(id, request.getUserId());
        existing.setNickname(request.getNickname().trim());
        existing.setBeneficiaryName(request.getBeneficiaryName().trim());
        existing.setAccountNumber(request.getAccountNumber().trim());
        existing.setBankName(request.getBankName().trim());
        existing.setIfscOrRouting(request.getIfscOrRouting().trim().toUpperCase());
        existing.setBeneficiaryType(request.getBeneficiaryType());
        return toResponse(beneficiaryRepository.save(existing));
    }

    /**
     * Soft-deletes by marking inactive (keeps history for past transfers).
     */
    @Transactional
    public BeneficiaryResponse deactivate(UUID id, UUID userId) {
        Beneficiary existing = requireOwned(id, userId);
        existing.setStatus(BeneficiaryStatus.INACTIVE);
        return toResponse(beneficiaryRepository.save(existing));
    }

    /**
     * Loads an active beneficiary owned by the user, or fails with a business error.
     */
    @Transactional(readOnly = true)
    public Beneficiary requireActiveOwned(UUID id, UUID userId) {
        Beneficiary beneficiary = requireOwned(id, userId);
        if (beneficiary.getStatus() != BeneficiaryStatus.ACTIVE) {
            throw new ValidationBusinessException(ErrorCodes.PAY_006);
        }
        return beneficiary;
    }

    private Beneficiary requireOwned(UUID id, UUID userId) {
        return beneficiaryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCodes.PAY_005));
    }

    private BeneficiaryResponse toResponse(Beneficiary b) {
        return BeneficiaryResponse.builder()
                .id(b.getId())
                .userId(b.getUserId())
                .nickname(b.getNickname())
                .beneficiaryName(b.getBeneficiaryName())
                .accountNumber(b.getAccountNumber())
                .bankName(b.getBankName())
                .ifscOrRouting(b.getIfscOrRouting())
                .beneficiaryType(b.getBeneficiaryType())
                .status(b.getStatus())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
