package com.project.backend.global.error;

public class CardOwnershipMismatchException extends RuntimeException {

    public CardOwnershipMismatchException(String cardId, String existingUserId, String requestedUserId) {
        super("Card ownership mismatch. cardId="
                + cardId
                + ", existingUserId="
                + existingUserId
                + ", requestedUserId="
                + requestedUserId);
    }
}
