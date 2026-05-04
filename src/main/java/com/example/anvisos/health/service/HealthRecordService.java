package com.example.anvisos.health.service;

import com.example.anvisos.health.dto.HealthRecordRequest;
import com.example.anvisos.model.entity.HealthRecord;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.repository.HealthRecordRepository;
import com.example.anvisos.model.repository.UserRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class HealthRecordService {
    private final HealthRecordRepository healthRecordRepository;
    private final UserRepository userRepository;

    public HealthRecordService(HealthRecordRepository healthRecordRepository, UserRepository userRepository) {
        this.healthRecordRepository = healthRecordRepository;
        this.userRepository = userRepository;
    }

    public HealthRecord upsert(Long userId, HealthRecordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        HealthRecord record = healthRecordRepository.findByUserId(userId)
                .orElseGet(() -> HealthRecord.builder()
                        .user(user)
                        .createdAt(Instant.now())
                        .build());

        record.setBloodType(request.getBloodType());
        record.setAllergies(request.getAllergies());
        record.setConditions(request.getConditions());
        record.setEmergencyNote(request.getEmergencyNote());
        record.setAvatarUrl(request.getAvatarUrl());
        record.setBirthYear(request.getBirthYear());
        record.setUpdatedAt(Instant.now());

        return healthRecordRepository.save(record);
    }

    public HealthRecord get(Long userId) {
        return healthRecordRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Health record not found"));
    }
}

