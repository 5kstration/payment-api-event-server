package com.project.backend.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "user_payment_states")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserPaymentState {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false, length = 26)
    private String userId;

    @Column(name = "budget_sync_enabled", nullable = false)
    private boolean budgetSyncEnabled;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    @Column(name = "last_flushed_at")
    private LocalDateTime lastFlushedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static UserPaymentState create(String userId) {
        LocalDateTime now = LocalDateTime.now();
        return UserPaymentState.builder()
                .userId(userId)
                .budgetSyncEnabled(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void activate(LocalDateTime activatedAt) {
        this.budgetSyncEnabled = true;
        this.activatedAt = activatedAt;
        this.updatedAt = activatedAt;
    }

    public void deactivate(LocalDateTime deactivatedAt) {
        this.budgetSyncEnabled = false;
        this.deactivatedAt = deactivatedAt;
        this.updatedAt = deactivatedAt;
    }

    public void markFlushed(LocalDateTime flushedAt) {
        this.lastFlushedAt = flushedAt;
        this.updatedAt = flushedAt;
    }
}
