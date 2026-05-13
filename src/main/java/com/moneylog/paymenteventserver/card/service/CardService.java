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

     /*
    Main 프로젝트가 제공하는 카드 데이터를 등록 Register method
    이미 등록된 카드 => 기존 카드를 반환
    등록되지 않은 카드 => 새로 저장 후 반환
      */
    @Transactional
    public CardResponse registerCard(RegisterCardRequest request) {
        return cardRepository
                .findByUserIdAndUserCardId(request.userId(), request.userCardId())
                .map(CardResponse::from)
                .orElseGet(() -> {
                    Card card = Card.builder()
                            .userId(request.userId())
                            .userCardId(request.userCardId())
                            .cardCompany(request.cardCompany())
                            .cardNumberLast4(request.cardNumberLast4())
                            .active(true)
                            .registeredAt(LocalDateTime.now())
                            .build();

                    Card savedCard = cardRepository.save(card);

                    return CardResponse.from(savedCard);
                });
    }


    public List<CardResponse> getCards() {
        return cardRepository.findAll()
                .stream()
                .map(CardResponse::from)
                .toList();
    }
}
