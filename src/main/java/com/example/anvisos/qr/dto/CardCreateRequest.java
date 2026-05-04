package com.example.anvisos.qr.dto;

import com.example.anvisos.model.enums.CardStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardCreateRequest {
    @NotNull
    private Long userId;

    private CardStatus status;
}
