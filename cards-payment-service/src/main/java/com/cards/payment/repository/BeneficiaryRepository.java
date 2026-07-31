package com.cards.payment.repository;

import com.cards.payment.domain.Beneficiary;
import com.cards.payment.domain.BeneficiaryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence for saved beneficiaries (payees).
 */
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {

    List<Beneficiary> findByUserIdOrderByNicknameAsc(UUID userId);

    List<Beneficiary> findByUserIdAndStatusOrderByNicknameAsc(UUID userId, BeneficiaryStatus status);

    Optional<Beneficiary> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndAccountNumberIgnoreCase(UUID userId, String accountNumber);
}
