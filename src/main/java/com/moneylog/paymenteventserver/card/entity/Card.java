package com.moneylog.paymenteventserver.card.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "cards",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_card",
                        columnNames = {"user_id", "user_card_id"}
                )
        }
)
// user_id + user_card_id 기준 중복 등록을 막기위해 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_card_id", nullable = false)
    private Long userCardId;

    @Column(name = "card_company", nullable = false)
    private String cardCompany;

    @Column(name = "card_number_last4", nullable = false, length = 4)
    private String cardNumberLast4;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    public void deactivate() {
        this.active = false;
    }
}