package com.project.backend.payment.dto;

import java.time.LocalDateTime;

public record PaymentEventPostRequest(
        String externalPaymentEventId,
        String userId,
        String cardId,
        String cardName,
        String cardCompany,
        String cardLast4,
        String merchantName,
        String category,
        Long amount,
        LocalDateTime paidAt
) {
}
