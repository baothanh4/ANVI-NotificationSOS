package com.example.anvisos.common;

import com.example.anvisos.common.dto.AuditLogResponse;
import com.example.anvisos.model.entity.AuditLog;
import com.example.anvisos.model.repository.AuditLogRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/cards/{cardId}")
    public List<AuditLogResponse> byCard(@PathVariable Long cardId) {
        return auditLogRepository.findByCardIdOrderByCreatedAtDesc(cardId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getUser() == null ? null : log.getUser().getId(),
                log.getCard() == null ? null : log.getCard().getId(),
                log.getEventType(),
                log.getIp(),
                log.getUserAgent(),
                log.getDeviceFingerprint(),
                log.getLocation(),
                log.getCreatedAt()
        );
    }
}

