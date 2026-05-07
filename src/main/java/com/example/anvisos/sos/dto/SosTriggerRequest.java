package com.example.anvisos.sos.dto;

import com.example.anvisos.model.enums.SosType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SosTriggerRequest {
    @NotNull
    private Long userId;
    private Long cardId;
    private BigDecimal gpsLat;
    private BigDecimal gpsLng;
    private String locationText;
    private String ipLocation;
    private String manualAddress;

    private boolean isSilent = false;
    private SosType sosType = SosType.MANUAL;

    public SosTriggerRequest() {}
}
