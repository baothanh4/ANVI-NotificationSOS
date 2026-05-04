package com.example.anvisos.health.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HealthRecordRequest {
    private String bloodType;
    private String allergies;
    private String conditions;
    private String emergencyNote;
    private String avatarUrl;
    private Integer birthYear;
}

