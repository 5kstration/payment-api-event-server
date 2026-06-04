package com.project.backend.payment.repository;

import com.project.backend.payment.entity.UserPaymentState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPaymentStateRepository extends JpaRepository<UserPaymentState, String> {

    boolean existsByUserIdAndBudgetSyncEnabledTrue(String userId);
}
