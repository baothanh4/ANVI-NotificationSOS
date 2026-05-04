package com.example.anvisos.health.service;

import com.example.anvisos.common.AuditService;
import com.example.anvisos.health.dto.AccessGrantRequest;
import com.example.anvisos.model.entity.AccessGrant;
import com.example.anvisos.model.entity.EmergencyContact;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.enums.AccessGrantStatus;
import com.example.anvisos.model.enums.AuditEventType;
import com.example.anvisos.model.repository.AccessGrantRepository;
import com.example.anvisos.model.repository.EmergencyContactRepository;
import com.example.anvisos.model.repository.UserRepository;
import com.example.anvisos.notification.NotificationService;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class AccessGrantService {
    private final AccessGrantRepository accessGrantRepository;
    private final UserRepository userRepository;
    private final EmergencyContactRepository contactRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public AccessGrantService(
            AccessGrantRepository accessGrantRepository,
            UserRepository userRepository,
            EmergencyContactRepository contactRepository,
            NotificationService notificationService,
            AuditService auditService
    ) {
        this.accessGrantRepository = accessGrantRepository;
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    public AccessGrant requestAccess(AccessGrantRequest request, String ip, String userAgent) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Instant now = Instant.now();
        AccessGrant grant = AccessGrant.builder()
                .user(user)
                .doctorName(request.getDoctorName())
                .hospitalName(request.getHospitalName())
                .status(AccessGrantStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        AccessGrant saved = accessGrantRepository.save(grant);
        notifyFamily(saved, "Yeu cau xem ho so y te dang cho phe duyet.");
        auditService.record(AuditEventType.ACCESS_REQUESTED, user, null, ip, userAgent, null, null);
        return saved;
    }

    public AccessGrant approve(Long grantId, String ip, String userAgent) {
        AccessGrant grant = accessGrantRepository.findById(grantId)
                .orElseThrow(() -> new IllegalArgumentException("Access grant not found"));

        if (grant.getStatus() != AccessGrantStatus.PENDING) {
            throw new IllegalStateException("Grant is not pending");
        }

        Instant now = Instant.now();
        grant.setStatus(AccessGrantStatus.APPROVED);
        grant.setToken(UUID.randomUUID().toString());
        grant.setExpiresAt(now.plusSeconds(24 * 60 * 60));
        grant.setUpdatedAt(now);

        AccessGrant saved = accessGrantRepository.save(grant);
        notifyEmitters(saved);
        auditService.record(AuditEventType.ACCESS_APPROVED, grant.getUser(), null, ip, userAgent, null, null);
        return saved;
    }

    public AccessGrant deny(Long grantId, String ip, String userAgent) {
        AccessGrant grant = accessGrantRepository.findById(grantId)
                .orElseThrow(() -> new IllegalArgumentException("Access grant not found"));

        if (grant.getStatus() != AccessGrantStatus.PENDING) {
            throw new IllegalStateException("Grant is not pending");
        }

        grant.setStatus(AccessGrantStatus.DENIED);
        grant.setUpdatedAt(Instant.now());

        AccessGrant saved = accessGrantRepository.save(grant);
        notifyEmitters(saved);
        auditService.record(AuditEventType.ACCESS_DENIED, grant.getUser(), null, ip, userAgent, null, null);
        return saved;
    }

    public AccessGrant get(Long grantId) {
        AccessGrant grant = accessGrantRepository.findById(grantId)
                .orElseThrow(() -> new IllegalArgumentException("Access grant not found"));

        if (grant.getStatus() == AccessGrantStatus.APPROVED
                && grant.getExpiresAt() != null
                && grant.getExpiresAt().isBefore(Instant.now())) {
            grant.setStatus(AccessGrantStatus.EXPIRED);
            grant.setUpdatedAt(Instant.now());
            grant = accessGrantRepository.save(grant);
        }

        return grant;
    }

    public SseEmitter subscribe(Long grantId) {
        SseEmitter emitter = new SseEmitter(60_000L);
        emitters.computeIfAbsent(grantId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> emitters.getOrDefault(grantId, new CopyOnWriteArrayList<>()).remove(emitter));
        emitter.onTimeout(() -> emitters.getOrDefault(grantId, new CopyOnWriteArrayList<>()).remove(emitter));
        return emitter;
    }

    private void notifyFamily(AccessGrant grant, String message) {
        List<EmergencyContact> contacts = contactRepository.findByUserIdOrderByPriorityAsc(grant.getUser().getId());
        for (EmergencyContact contact : contacts) {
            notificationService.sendToPhone(contact.getPhone(), message);
        }
    }

    private void notifyEmitters(AccessGrant grant) {
        List<SseEmitter> grantEmitters = emitters.get(grant.getId());
        if (grantEmitters == null) {
            return;
        }
        for (SseEmitter emitter : grantEmitters) {
            try {
                emitter.send(SseEmitter.event().name("status").data(grant.getStatus().name()));
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        }
    }
}
