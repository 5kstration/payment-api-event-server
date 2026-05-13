package com.moneylog.paymenteventserver.card.dto;


import com.moneylog.paymenteventserver.card.entity.Card;

import java.time.LocalDateTime;

public record CardResponse(
        Long id,
        Long userId,
        Long userCardId,
        String cardCompany,
        String cardNumberLast4,
        boolean active,
        LocalDateTime registeredAt
) {
    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getId(),
                card.getUserId(),
                card.getUserCardId(),
                card.getCardCompany(),
                card.getCardNumberLast4(),
                card.isActive(),
                card.getRegisteredAt()
        );
    }
}