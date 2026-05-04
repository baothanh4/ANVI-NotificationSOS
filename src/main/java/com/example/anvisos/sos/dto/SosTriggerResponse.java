package com.example.anvisos.sos.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SosTriggerResponse {
    private int totalRecipients;
    private String publicToken;   // Token để tạo link /sos-alert/{token}
}

