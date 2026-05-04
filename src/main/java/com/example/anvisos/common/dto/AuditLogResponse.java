package com.example.anvisos.common.dto;

import com.example.anvisos.model.enums.AuditEventType;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private Long cardId;
    private AuditEventType eventType;
    private String ip;
    private String userAgent;
    private String deviceFingerprint;
    private String location;
    private Instant createdAt;
}
