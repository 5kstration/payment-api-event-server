package com.project.backend.card.controller;

import com.project.backend.card.dto.CardResponse;
import com.project.backend.card.dto.RegisterCardRequest;
import com.project.backend.card.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping
    public CardResponse registerCard(@Valid @RequestBody RegisterCardRequest request) {
        return cardService.registerCard(request);
    }

    @GetMapping
    public List<CardResponse> getCards() {
        return cardService.getCards();
    }
}
