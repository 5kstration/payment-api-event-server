package com.project.backend.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(
        name = "payment_event_outbox",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_event_outbox_external_payment_event_id",
                        columnNames = {"external_payment_event_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PaymentEventOutbox {

    @Id
    @Column(name = "outbox_id", nullable = false, updatable = false, length = 36)
    private String outboxId;

    @Column(name = "payment_event_id", nullable = false, updatable = false, length = 36)
    private String paymentEventId;

    @Column(name = "external_payment_event_id", nullable = false, updatable = false, length = 36)
    private String externalPaymentEventId;

    @Column(name = "subject", nullable = false, length = 100)
    private String subject;

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentEventOutboxStatus status;

    @Column(name = "publish_attempts", nullable = false)
    private int publishAttempts;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public static PaymentEventOutbox create(
            PaymentEvent event,
            String subject,
            String payload,
            LocalDateTime now
    ) {
        return PaymentEventOutbox.builder()
                .outboxId(UUID.randomUUID().toString())
                .paymentEventId(event.getPaymentEventId())
                .externalPaymentEventId(event.getExternalPaymentEventId())
                .subject(subject)
                .payload(payload)
                .status(PaymentEventOutboxStatus.PENDING)
                .publishAttempts(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public void markPublished(LocalDateTime publishedAt) {
        this.status = PaymentEventOutboxStatus.PUBLISHED;
        this.publishedAt = publishedAt;
        this.updatedAt = publishedAt;
        this.lastError = null;
    }

    public void recordFailure(String message, LocalDateTime failedAt) {
        this.publishAttempts++;
        this.lastError = trim(message);
        this.updatedAt = failedAt;
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}
