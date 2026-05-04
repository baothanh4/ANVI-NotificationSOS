package com.example.anvisos.qr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QrResolveResponse {
    private Long cardId;
    private Long userId;
    private boolean active;
}

