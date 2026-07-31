package com.cards.payment.repository;

import com.cards.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for persisting and loading {@link Payment} entities.
 */
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Payment> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}

