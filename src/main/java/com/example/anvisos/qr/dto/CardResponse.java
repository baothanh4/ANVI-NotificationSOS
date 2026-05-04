package com.example.anvisos.qr.dto;

import com.example.anvisos.model.enums.CardStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CardResponse {
    private Long id;
    private Long userId;
    private CardStatus status;
    private Instant createdAt;
}
