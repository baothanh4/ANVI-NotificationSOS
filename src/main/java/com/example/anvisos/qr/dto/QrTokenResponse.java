package com.example.anvisos.qr.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QrTokenResponse {
    private Long id;
    private String token;
    private String shortCode;
    private String shortUrl;
    private boolean active;
    private Instant createdAt;
}

