package com.project.backend.payment.entity;

import com.project.backend.card.entity.Card;
import com.project.backend.payment.dto.PaymentEventPostRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
        name = "payment_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_events_external_payment_event_id",
                        columnNames = {"external_payment_event_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PaymentEvent {

    @Id
    @Column(name = "payment_event_id", nullable = false, updatable = false, length = 36)
    private String paymentEventId;

    @Column(name = "external_payment_event_id", nullable = false, updatable = false, length = 36)
    private String externalPaymentEventId;

    @Column(name = "user_id", nullable = false, length = 26)
    private String userId;

    @Column(name = "card_id", nullable = false, length = 26)
    private String cardId;

    @Column(name = "card_name", nullable = false, length = 50)
    private String cardName;

    @Column(name = "card_company", nullable = false, length = 30)
    private String cardCompany;

    @Column(name = "card_last4", nullable = false, length = 4)
    private String cardLast4;

    @Column(name = "merchant_name", nullable = false, length = 100)
    private String merchantName;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Column(name = "sent_to_budget", nullable = false)
    private boolean sentToBudget;

    @Column(name = "sent_to_budget_at")
    private LocalDateTime sentToBudgetAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static PaymentEvent create(
            Card card,
            String merchantName,
            String category,
            Long amount,
            LocalDateTime paidAt
    ) {
        return PaymentEvent.builder()
                .paymentEventId(UUID.randomUUID().toString())
                .externalPaymentEventId(UUID.randomUUID().toString())
                .userId(card.getUserId())
                .cardId(card.getCardId())
                .cardName(card.getCardName())
                .cardCompany(card.getCardCompany())
                .cardLast4(card.getCardLast4())
                .merchantName(merchantName)
                .category(category)
                .amount(amount)
                .paidAt(paidAt)
                .sentToBudget(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public PaymentEventPostRequest toPostRequest() {
        return new PaymentEventPostRequest(
                externalPaymentEventId,
                userId,
                cardId,
                cardName,
                cardCompany,
                cardLast4,
                merchantName,
                category,
                amount,
                paidAt
        );
    }

    public void markSentToBudget(LocalDateTime sentAt) {
        this.sentToBudget = true;
        this.sentToBudgetAt = sentAt;
    }
}
