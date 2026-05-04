package com.example.anvisos.model.repository;

import com.example.anvisos.model.entity.HealthRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    Optional<HealthRecord> findByUserId(Long userId);
}

