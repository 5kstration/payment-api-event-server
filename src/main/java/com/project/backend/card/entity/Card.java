package com.project.backend.card.entity;

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

@Getter
@Entity
@Table(
        name = "cards",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cards_user_id",
                        columnNames = {"user_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @Column(name = "card_id", nullable = false, updatable = false, length = 26)
    private String cardId;

    @Column(name = "user_id", nullable = false, length = 26)
    private String userId;

    @Column(name = "card_name", nullable = false, length = 50)
    private String cardName;

    @Column(name = "card_company", nullable = false, length = 30)
    private String cardCompany;

    @Column(name = "card_last4", nullable = false, length = 4)
    private String cardLast4;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    public void syncFromOnboarding(String cardName, String cardCompany, String cardLast4) {
        this.cardName = cardName;
        this.cardCompany = cardCompany;
        this.cardLast4 = cardLast4;
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }
}
