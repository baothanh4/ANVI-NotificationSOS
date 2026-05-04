package com.example.anvisos.qr.service;

import com.example.anvisos.common.AuditService;
import com.example.anvisos.model.entity.Card;
import com.example.anvisos.model.entity.QrToken;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.enums.CardStatus;
import com.example.anvisos.model.repository.CardRepository;
import com.example.anvisos.model.repository.QrTokenRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QrServiceTest {
    @Mock
    private CardRepository cardRepository;

    @Mock
    private QrTokenRepository qrTokenRepository;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    @Mock
    private AuditService auditService;

    private QrService qrService;

    @Test
    void createTokenUsesShortCode() {
        User user = User.builder().id(1L).build();
        Card card = Card.builder()
                .id(10L)
                .user(user)
                .status(CardStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        qrService = new QrService(cardRepository, qrTokenRepository, shortCodeGenerator, auditService, "http://localhost/qr/");
        Mockito.when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        Mockito.when(shortCodeGenerator.generate()).thenReturn("Abc12345");
        Mockito.when(qrTokenRepository.findByShortCode("Abc12345")).thenReturn(Optional.empty());
        Mockito.when(qrTokenRepository.save(ArgumentMatchers.any(QrToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QrToken token = qrService.createToken(card.getId(), true);

        Assertions.assertEquals("Abc12345", token.getShortCode());
    }

    @Test
    void buildShortUrlKeepsTrailingSlash() {
        qrService = new QrService(cardRepository, qrTokenRepository, shortCodeGenerator, auditService, "http://localhost/qr/");
        String url = qrService.buildShortUrl("XyZ");
        Assertions.assertEquals("http://localhost/qr/XyZ", url);
    }
}
