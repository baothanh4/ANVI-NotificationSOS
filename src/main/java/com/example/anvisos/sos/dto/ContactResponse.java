package com.example.anvisos.sos.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ContactResponse {
    private Long id;
    private String name;
    private String phone;
    private int priority;
    private boolean verified;
}

