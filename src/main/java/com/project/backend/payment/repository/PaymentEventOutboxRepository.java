package com.project.backend.payment.repository;

import com.project.backend.payment.entity.PaymentEventOutbox;
import com.project.backend.payment.entity.PaymentEventOutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentEventOutboxRepository extends JpaRepository<PaymentEventOutbox, String> {

    boolean existsByExternalPaymentEventId(String externalPaymentEventId);

    Optional<PaymentEventOutbox> findByExternalPaymentEventId(String externalPaymentEventId);

    List<PaymentEventOutbox> findTop50ByStatusOrderByCreatedAtAsc(PaymentEventOutboxStatus status);
}
