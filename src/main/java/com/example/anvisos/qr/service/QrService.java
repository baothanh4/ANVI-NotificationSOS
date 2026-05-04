package com.example.anvisos.qr.service;

import com.example.anvisos.common.AuditService;
import com.example.anvisos.model.entity.Card;
import com.example.anvisos.model.entity.QrToken;
import com.example.anvisos.model.enums.AuditEventType;
import com.example.anvisos.model.repository.CardRepository;
import com.example.anvisos.model.repository.QrTokenRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class QrService {
    private final CardRepository cardRepository;
    private final QrTokenRepository qrTokenRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final AuditService auditService;
    private final String qrBaseUrl;

    public QrService(
            CardRepository cardRepository,
            QrTokenRepository qrTokenRepository,
            ShortCodeGenerator shortCodeGenerator,
            AuditService auditService,
            @Value("${anvi.qr.base-url}") String qrBaseUrl
    ) {
        this.cardRepository = cardRepository;
        this.qrTokenRepository = qrTokenRepository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.auditService = auditService;
        this.qrBaseUrl = qrBaseUrl;
    }

    public QrToken createToken(Long cardId, boolean active) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));

        String shortCode = generateUniqueShortCode();
        QrToken token = QrToken.builder()
                .card(card)
                .token(UUID.randomUUID().toString())
                .shortCode(shortCode)
                .active(active)
                .createdAt(Instant.now())
                .build();
        return qrTokenRepository.save(token);
    }

    public QrToken resolveShortCode(
            String shortCode,
            String ip,
            String userAgent,
            String deviceFingerprint,
            String location
    ) {
        QrToken token = qrTokenRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("QR code not found"));

        auditService.record(
                AuditEventType.QR_SCAN,
                token.getCard().getUser(),
                token.getCard(),
                ip,
                userAgent,
                deviceFingerprint,
                location
        );

        return token;
    }

    public String buildShortUrl(String shortCode) {
        if (qrBaseUrl.endsWith("/")) {
            return qrBaseUrl + shortCode;
        }
        return qrBaseUrl + "/" + shortCode;
    }

    private String generateUniqueShortCode() {
        for (int i = 0; i < 5; i++) {
            String candidate = shortCodeGenerator.generate();
            if (qrTokenRepository.findByShortCode(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new IllegalStateException("Failed to generate unique short code");
    }
}
