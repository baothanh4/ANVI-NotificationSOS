package com.example.anvisos.model.repository;

import com.example.anvisos.model.entity.QrToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QrTokenRepository extends JpaRepository<QrToken, Long> {
    Optional<QrToken> findByShortCode(String shortCode);
    Optional<QrToken> findByToken(String token);
    java.util.List<QrToken> findByCardId(Long cardId);
}

