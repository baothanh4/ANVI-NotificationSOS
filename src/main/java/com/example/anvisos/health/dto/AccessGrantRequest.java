package com.example.anvisos.health.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessGrantRequest {
    @NotNull
    private Long userId;

    @NotBlank
    private String doctorName;

    @NotBlank
    private String hospitalName;
}

