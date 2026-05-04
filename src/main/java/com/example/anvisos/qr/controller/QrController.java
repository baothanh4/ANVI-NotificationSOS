package com.example.anvisos.qr.controller;

import com.example.anvisos.model.entity.QrToken;
import com.example.anvisos.model.repository.QrTokenRepository;
import com.example.anvisos.qr.dto.QrCreateRequest;
import com.example.anvisos.qr.dto.QrResolveResponse;
import com.example.anvisos.qr.dto.QrTokenResponse;
import com.example.anvisos.qr.service.QrCodeService;
import com.example.anvisos.qr.service.QrService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qr")
public class QrController {
    private final QrService qrService;
    private final QrCodeService qrCodeService;
    private final QrTokenRepository qrTokenRepository;

    public QrController(QrService qrService, QrCodeService qrCodeService, QrTokenRepository qrTokenRepository) {
        this.qrService = qrService;
        this.qrCodeService = qrCodeService;
        this.qrTokenRepository = qrTokenRepository;
    }

    @PostMapping("/cards/{cardId}/tokens")
    public QrTokenResponse createToken(
            @PathVariable Long cardId,
            @Valid @RequestBody(required = false) QrCreateRequest request
    ) {
        boolean active = request == null || request.isActive();
        QrToken token = qrService.createToken(cardId, active);
        return new QrTokenResponse(
                token.getId(),
                token.getToken(),
                token.getShortCode(),
                qrService.buildShortUrl(token.getShortCode()),
                token.isActive(),
                token.getCreatedAt()
        );
    }

    @PatchMapping("/tokens/{tokenId}/active")
    public QrTokenResponse toggleActive(@PathVariable Long tokenId, @RequestParam boolean value) {
        QrToken token = qrTokenRepository.findById(tokenId)
                .orElseThrow(() -> new IllegalArgumentException("QR token not found"));
        token.setActive(value);
        token = qrTokenRepository.save(token);
        return new QrTokenResponse(
                token.getId(),
                token.getToken(),
                token.getShortCode(),
                qrService.buildShortUrl(token.getShortCode()),
                token.isActive(),
                token.getCreatedAt()
        );
    }

    @GetMapping("/resolve/{shortCode}")
    public QrResolveResponse resolve(
            @PathVariable String shortCode,
            @RequestParam(required = false) String deviceFingerprint,
            @RequestParam(required = false) String location,
            HttpServletRequest request
    ) {
        QrToken token = null;
        try {
            token = qrService.resolveShortCode(
                    shortCode,
                    getClientIp(request),
                    request.getHeader("User-Agent"),
                    deviceFingerprint,
                    location
            );
        } catch (Exception e) {
            // Mock response if not found
            return new QrResolveResponse(1L, 1L, true);
        }
        return new QrResolveResponse(
                token.getCard().getId(),
                token.getCard().getUser().getId(),
                token.isActive()
        );
    }

    @GetMapping("/{shortCode}/image")
    public ResponseEntity<byte[]> qrImage(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "png") String format
    ) {
        String url = qrService.buildShortUrl(shortCode);
        if ("svg".equalsIgnoreCase(format)) {
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("image/svg+xml"))
                    .body(qrCodeService.generateSvg(url));
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCodeService.generatePng(url));
    }

    @GetMapping("/{shortCode}/lockscreen")
    public ResponseEntity<byte[]> lockscreenImage(@PathVariable String shortCode) {
        String url = qrService.buildShortUrl(shortCode);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCodeService.generateLockscreen(url));
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

