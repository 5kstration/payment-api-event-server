package com.project.backend.global.error;

public record ErrorResponse(
        String code,
        String message
) {
}
