package com.example.anvisos.auth.service;

import com.example.anvisos.model.entity.RefreshToken;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTtlSeconds;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    public RefreshToken create(User user) {
        Instant now = Instant.now();
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .createdAt(now)
                .expiresAt(now.plusSeconds(refreshTtlSeconds))
                .build();
        return refreshTokenRepository.save(token);
    }

    public RefreshToken validate(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token expired");
        }
        return refreshToken;
    }

    public long getRefreshTtlSeconds() {
        return refreshTtlSeconds;
    }
}

