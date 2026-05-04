package com.example.anvisos.sos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String phone;

    @Min(1)
    @Max(3)
    private int priority;

    private boolean verified;
}

