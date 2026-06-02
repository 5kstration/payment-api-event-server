package com.moneylog.paymenteventserver.card.service;

import com.moneylog.paymenteventserver.card.dto.CardResponse;
import com.moneylog.paymenteventserver.card.dto.RegisterCardRequest;
import com.moneylog.paymenteventserver.card.entity.Card;
import com.moneylog.paymenteventserver.card.repository.CardRepository;
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

    @Transactional
    public CardResponse registerCard(RegisterCardRequest request) {
        return cardRepository.findById(request.cardId())
                .map(card -> syncExistingCard(card, request))
                .orElseGet(() -> cardRepository.findByUserId(request.userId())
                        .map(existingCard -> replaceUserCard(existingCard, request))
                        .orElseGet(() -> createCard(request)));
    }

    public List<CardResponse> getCards() {
        return cardRepository.findAll()
                .stream()
                .map(CardResponse::from)
                .toList();
    }

    private CardResponse syncExistingCard(Card card, RegisterCardRequest request) {
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
