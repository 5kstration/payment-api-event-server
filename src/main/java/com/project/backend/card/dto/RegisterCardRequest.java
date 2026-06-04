package com.project.backend.card.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterCardRequest(

        @NotBlank
        String cardId,

        @NotBlank
        String userId,

        @NotBlank
        String cardName,

        @NotBlank
        String cardCompany,

        @NotBlank
        @Pattern(regexp = "\\d{4}", message = "Card last4 must be exactly 4 digits.")
        String cardLast4

) {
}
