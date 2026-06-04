package com.project.backend.payment.repository;

import com.project.backend.payment.entity.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentEventRepository extends JpaRepository<PaymentEvent, String> {

    List<PaymentEvent> findByUserIdAndSentToBudgetFalseOrderByPaidAtAsc(String userId);
}
