package com.moneylog.paymenteventserver.card.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegisterCardRequest(

        @NotNull
        Long userId,

        @NotNull
        Long userCardId,

        @NotBlank
        String cardCompany,

        @NotBlank
        @Pattern(regexp = "\\d{4}", message = "카드 마지막 4자리는 숫자 4자리여야 합니다.")
        String cardNumberLast4

) {
}
