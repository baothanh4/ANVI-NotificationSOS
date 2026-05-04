package com.example.anvisos.model.repository;

import com.example.anvisos.model.entity.AuditLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByCardIdOrderByCreatedAtDesc(Long cardId);
}

