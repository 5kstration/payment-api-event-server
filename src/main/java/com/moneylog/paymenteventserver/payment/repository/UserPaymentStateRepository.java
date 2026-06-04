package com.moneylog.paymenteventserver.payment.repository;

import com.moneylog.paymenteventserver.payment.entity.UserPaymentState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPaymentStateRepository extends JpaRepository<UserPaymentState, String> {

    boolean existsByUserIdAndBudgetSyncEnabledTrue(String userId);
}
