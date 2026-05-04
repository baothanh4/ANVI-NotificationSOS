package com.example.anvisos.health.dto;

import com.example.anvisos.model.enums.AccessGrantStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccessGrantResponse {
    private Long id;
    private Long userId;
    private String doctorName;
    private String hospitalName;
    private AccessGrantStatus status;
    private String token;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
}
