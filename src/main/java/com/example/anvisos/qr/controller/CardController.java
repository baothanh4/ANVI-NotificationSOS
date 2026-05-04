package com.example.anvisos.qr.controller;

import com.example.anvisos.model.entity.Card;
import com.example.anvisos.qr.dto.CardCreateRequest;
import com.example.anvisos.qr.dto.CardResponse;
import com.example.anvisos.qr.service.CardService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards")
public class CardController {
    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public CardResponse create(@Valid @RequestBody CardCreateRequest request) {
        return toResponse(cardService.create(request));
    }

    @GetMapping
    public List<CardResponse> list(@RequestParam Long userId) {
        return cardService.listByUser(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CardResponse toResponse(Card card) {
        return new CardResponse(card.getId(), card.getUser().getId(), card.getStatus(), card.getCreatedAt());
    }
}

