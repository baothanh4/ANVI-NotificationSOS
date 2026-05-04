package com.example.anvisos.qr.service;

import com.example.anvisos.model.entity.Card;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.enums.CardStatus;
import com.example.anvisos.model.repository.CardRepository;
import com.example.anvisos.model.repository.UserRepository;
import com.example.anvisos.qr.dto.CardCreateRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public CardService(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    public Card create(CardCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Card card = Card.builder()
                .user(user)
                .status(request.getStatus() == null ? CardStatus.ACTIVE : request.getStatus())
                .createdAt(Instant.now())
                .build();
        return cardRepository.save(card);
    }

    public List<Card> listByUser(Long userId) {
        return cardRepository.findByUserId(userId);
    }
}
