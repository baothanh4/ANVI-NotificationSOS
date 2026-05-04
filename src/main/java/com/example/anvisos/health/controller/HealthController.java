package com.example.anvisos.health.controller;

import com.example.anvisos.health.dto.HealthRecordRequest;
import com.example.anvisos.health.dto.HealthRecordResponse;
import com.example.anvisos.health.service.HealthRecordService;
import com.example.anvisos.model.entity.HealthRecord;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    private final HealthRecordService healthRecordService;

    public HealthController(HealthRecordService healthRecordService) {
        this.healthRecordService = healthRecordService;
    }

    @PutMapping("/users/{userId}")
    public HealthRecordResponse upsert(@PathVariable Long userId, @Valid @RequestBody HealthRecordRequest request) {
        return toResponse(healthRecordService.upsert(userId, request));
    }

    @GetMapping("/users/{userId}")
    public HealthRecordResponse get(@PathVariable Long userId) {
        return toResponse(healthRecordService.get(userId));
    }

    private HealthRecordResponse toResponse(HealthRecord record) {
        return new HealthRecordResponse(
                record.getId(),
                record.getUser().getId(),
                record.getBloodType(),
                record.getAllergies(),
                record.getConditions(),
                record.getEmergencyNote(),
                record.getAvatarUrl(),
                record.getBirthYear(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}

