package com.example.anvisos.common;

import com.example.anvisos.model.entity.AuditLog;
import com.example.anvisos.model.entity.Card;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.enums.AuditEventType;
import com.example.anvisos.model.repository.AuditLogRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLog record(
            AuditEventType eventType,
            User user,
            Card card,
            String ip,
            String userAgent,
            String deviceFingerprint,
            String location
    ) {
        AuditLog log = AuditLog.builder()
                .eventType(eventType)
                .user(user)
                .card(card)
                .ip(ip)
                .userAgent(userAgent)
                .deviceFingerprint(deviceFingerprint)
                .location(location)
                .createdAt(Instant.now())
                .build();
        return auditLogRepository.save(log);
    }
}
