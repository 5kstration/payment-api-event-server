package com.moneylog.paymenteventserver.payment.dto;

import java.time.LocalDateTime;

public record PaymentEventPostRequest(
        String externalPaymentEventId,
        Long userId,
        Long userCardId,
        String cardCompany,
        String cardNumberLast4,
        String merchantName,
        String category,
        Long amount,
        LocalDateTime paidAt
) {
}