package com.moneylog.paymenteventserver.card.controller;

import com.moneylog.paymenteventserver.card.dto.CardResponse;
import com.moneylog.paymenteventserver.card.dto.RegisterCardRequest;
import com.moneylog.paymenteventserver.card.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    // 카드 등록 Controller
    @PostMapping
    public CardResponse registerCard(@Valid @RequestBody RegisterCardRequest request) {
        return cardService.registerCard(request);
    }

    // 카드 조회 Controller
    @GetMapping
    public List<CardResponse> getCards() {
        return cardService.getCards();
    }
}
