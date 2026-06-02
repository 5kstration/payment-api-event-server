package com.moneylog.paymenteventserver.global.error;

public record ErrorResponse(
        String code,
        String message
) {
}
