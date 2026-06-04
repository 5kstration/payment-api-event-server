package com.project.backend.card.service;

import com.project.backend.card.dto.CardResponse;
import com.project.backend.card.dto.RegisterCardRequest;
import com.project.backend.card.entity.Card;
import com.project.backend.card.repository.CardRepository;
import com.project.backend.global.error.CardOwnershipMismatchException;
import com.project.backend.payment.service.UserPaymentStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardService {

    private final CardRepository cardRepository;
    private final UserPaymentStateService userPaymentStateService;

    @Transactional
    public CardResponse registerCard(RegisterCardRequest request) {
        CardResponse response = cardRepository.findById(request.cardId())
                .map(card -> syncExistingCard(card, request))
                .orElseGet(() -> cardRepository.findByUserId(request.userId())
                        .map(existingCard -> replaceUserCard(existingCard, request))
                        .orElseGet(() -> createCard(request)));
        userPaymentStateService.activateBudgetSync(request.userId());
        return response;
    }

    public List<CardResponse> getCards() {
        return cardRepository.findAll()
                .stream()
                .map(CardResponse::from)
                .toList();
    }

    private CardResponse syncExistingCard(Card card, RegisterCardRequest request) {
        if (!card.getUserId().equals(request.userId())) {
            throw new CardOwnershipMismatchException(
                    card.getCardId(),
                    card.getUserId(),
                    request.userId()
            );
        }

        card.syncFromOnboarding(
                request.cardName(),
                request.cardCompany(),
                request.cardLast4()
        );
        return CardResponse.from(card);
    }

    private CardResponse replaceUserCard(Card existingCard, RegisterCardRequest request) {
        cardRepository.delete(existingCard);
        cardRepository.flush();
        return createCard(request);
    }

    private CardResponse createCard(RegisterCardRequest request) {
        Card card = Card.builder()
                .cardId(request.cardId())
                .userId(request.userId())
                .cardName(request.cardName())
                .cardCompany(request.cardCompany())
                .cardLast4(request.cardLast4())
                .active(true)
                .registeredAt(LocalDateTime.now())
                .build();

        Card savedCard = cardRepository.save(card);

        return CardResponse.from(savedCard);
    }
}
