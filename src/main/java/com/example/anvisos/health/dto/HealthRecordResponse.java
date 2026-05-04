package com.example.anvisos.health.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HealthRecordResponse {
    private Long id;
    private Long userId;
    private String bloodType;
    private String allergies;
    private String conditions;
    private String emergencyNote;
    private String avatarUrl;
    private Integer birthYear;
    private Instant createdAt;
    private Instant updatedAt;
}

