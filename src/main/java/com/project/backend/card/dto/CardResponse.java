package com.project.backend.card.dto;

import com.project.backend.card.entity.Card;

import java.time.LocalDateTime;

public record CardResponse(
        String cardId,
        String userId,
        String cardName,
        String cardCompany,
        String cardLast4,
        boolean active,
        LocalDateTime registeredAt
) {
    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getCardId(),
                card.getUserId(),
                card.getCardName(),
                card.getCardCompany(),
                card.getCardLast4(),
                card.isActive(),
                card.getRegisteredAt()
        );
    }
}
