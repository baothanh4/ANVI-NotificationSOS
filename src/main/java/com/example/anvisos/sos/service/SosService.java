package com.example.anvisos.sos.service;

import com.example.anvisos.common.AuditService;
import com.example.anvisos.model.entity.*;
import com.example.anvisos.model.enums.AuditEventType;
import com.example.anvisos.model.repository.*;
import com.example.anvisos.notification.EmailService;
import com.example.anvisos.notification.NotificationService;
import com.example.anvisos.sos.dto.SosAlertPublicResponse;
import com.example.anvisos.sos.dto.SosTriggerRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Service
public class SosService {

    private static final DateTimeFormatter VN_FMT =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final EmergencyContactRepository contactRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final SosEventRepository sosEventRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final AuditService auditService;
    private final SocialLinkRepository socialLinkRepository;
    private final RescueConnectionRepository rescueConnectionRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Value("${anvi.qr.base-url:http://localhost:8081/}")
    private String baseUrl;

    public SosService(
            UserRepository userRepository,
            CardRepository cardRepository,
            EmergencyContactRepository contactRepository,
            HealthRecordRepository healthRecordRepository,
            SosEventRepository sosEventRepository,
            NotificationService notificationService,
            EmailService emailService,
            AuditService auditService,
            SocialLinkRepository socialLinkRepository,
            RescueConnectionRepository rescueConnectionRepository,
            org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate
    ) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.contactRepository = contactRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.sosEventRepository = sosEventRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.auditService = auditService;
        this.socialLinkRepository = socialLinkRepository;
        this.rescueConnectionRepository = rescueConnectionRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // ─────────────────────────────────────────────
    //  Trigger SOS
    // ─────────────────────────────────────────────

    @Transactional
    public TriggerResult trigger(SosTriggerRequest request, String ip, String userAgent) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Card card = request.getCardId() != null
                ? cardRepository.findById(request.getCardId()).orElse(null)
                : null;

        List<EmergencyContact> contacts = contactRepository.findByUserIdOrderByPriorityAsc(user.getId());
        HealthRecord health = healthRecordRepository.findByUserId(user.getId()).orElse(null);

        // Tạo hoặc cập nhật SosEvent
        SosEvent event = createOrUpdateSosEvent(user, request);
        String alertUrl = buildAlertUrl(event.getPublicToken());

        // SMS message
        String smsMessage = buildSmsMessage(user, health, request, alertUrl);

        int sent = 0;
        // 2b. Notify Rescue Network (Social Connections)
        List<RescueConnection> sentConns = rescueConnectionRepository.findByRequesterIdAndStatus(user.getId(), "ACCEPTED");
        List<RescueConnection> receivedConns = rescueConnectionRepository.findByTargetIdAndStatus(user.getId(), "ACCEPTED");

        for (RescueConnection conn : sentConns) {
            notifyContact(conn.getTarget(), user, health, request, alertUrl, smsMessage, event.getPublicToken());
            sent++;
        }
        for (RescueConnection conn : receivedConns) {
            notifyContact(conn.getRequester(), user, health, request, alertUrl, smsMessage, event.getPublicToken());
            sent++;
        }

        // Gửi 1 bản sao Email cho chính nạn nhân
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            double lat = request.getGpsLat() != null ? request.getGpsLat().doubleValue() : 0;
            double lng = request.getGpsLng() != null ? request.getGpsLng().doubleValue() : 0;
            emailService.sendSosAlert(
                    user.getEmail(),
                    user.getFullName(),
                    user.getPhone(),
                    lat, lng,
                    health != null ? health.getBloodType() : null,
                    health != null ? health.getAllergies() : null,
                    health != null ? health.getConditions() : null,
                    alertUrl
            );
        }

        auditService.record(AuditEventType.SOS_TRIGGERED, user, card, ip, userAgent, null, request.getLocationText());

        // 3. PHÁT TÍN HIỆU REAL-TIME QUA WEBSOCKET CHO ADMIN/DASHBOARD
        var alertMap = new java.util.HashMap<String, Object>();
        alertMap.put("type", "SOS_ALERT");
        alertMap.put("victimName", user.getFullName());
        alertMap.put("publicToken", event.getPublicToken());
        alertMap.put("lat", event.getLastLat());
        alertMap.put("lng", event.getLastLng());
        alertMap.put("locationText", event.getLocationText());
        alertMap.put("sosType", event.getSosType());
        alertMap.put("isSilent", event.isSilent());
        messagingTemplate.convertAndSend("/topic/sos-alerts", alertMap, (java.util.Map<String, Object>) null);
        
        // 4. THÔNG BÁO CHO TÌNH NGUYỆN VIÊN GẦN ĐÓ (LOCAL HEROES)
        if (request.getGpsLat() != null && request.getGpsLng() != null) {
            List<User> volunteers = userRepository.findVolunteersNearby(request.getGpsLat(), request.getGpsLng(), 1.0); // 1km
            for (User volunteer : volunteers) {
                if (!volunteer.getId().equals(user.getId())) {
                    messagingTemplate.convertAndSendToUser(volunteer.getPhone(), "/queue/nearby-sos", java.util.Map.of(
                        "message", "Có người cần giúp đỡ gần vị trí của bạn!",
                        "victimName", user.getFullName(),
                        "distance", "Dưới 1km",
                        "alertUrl", alertUrl
                    ));
                }
            }
        }

        return new TriggerResult(sent, event.getPublicToken());
    }

    // ─────────────────────────────────────────────
    //  Public Alert Info (no auth required)
    // ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public SosAlertPublicResponse getPublicAlert(String token) {
        SosEvent event = sosEventRepository.findByPublicTokenAndActiveTrue(token)
                .orElseThrow(() -> new IllegalArgumentException("SOS alert not found or expired"));

        // Lấy User trực tiếp từ repo để tránh lỗi Lazy proxy từ SosEvent
        User user = userRepository.findById(event.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        HealthRecord health = healthRecordRepository.findByUserId(user.getId()).orElse(null);

        SosAlertPublicResponse resp = new SosAlertPublicResponse();
        resp.setVictimName(user.getFullName());
        resp.setVictimPhone(user.getPhone());
        resp.setInitials(buildInitials(user.getFullName()));
        resp.setAvatarUrl(health != null ? health.getAvatarUrl() : null);
        resp.setActive(event.isActive());
        resp.setTriggeredAt(VN_FMT.format(event.getTriggeredAt()));
        resp.setUpdatedAt(VN_FMT.format(event.getUpdatedAt()));
        resp.setLocationText(event.getLocationText());
        resp.setMediaUrl(event.getMediaUrl());
        resp.setSosType(event.getSosType());
        resp.setSafe(!event.isActive());

        if (health != null) {
            resp.setBloodType(health.getBloodType());
            resp.setAllergies(health.getAllergies());
            resp.setConditions(health.getConditions());
            resp.setEmergencyNote(health.getEmergencyNote());
            resp.setBirthYear(health.getBirthYear());
        }

        BigDecimal lat = event.getLastLat();
        BigDecimal lng = event.getLastLng();
        resp.setLat(lat);
        resp.setLng(lng);

        if (lat != null && lng != null) {
            resp.setGoogleMapsViewUrl(
                    "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng);
            resp.setGoogleMapsDirectionsUrl(
                    "https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng + "&travelmode=driving");
        }

        // Fetch visible social links
        List<SocialLink> socialLinks = socialLinkRepository.findByUserIdAndVisibleTrue(user.getId());
        resp.setSocialLinks(socialLinks.stream()
                .map(l -> com.example.anvisos.social.dto.SocialLinkResponse.builder()
                        .id(l.getId())
                        .platform(l.getPlatform())
                        .url(l.getUrl())
                        .visible(l.isVisible())
                        .build())
                .collect(java.util.stream.Collectors.toList()));

        return resp;
    }

    // ─────────────────────────────────────────────
    //  Update location (nạn nhân di chuyển)
    // ─────────────────────────────────────────────

    @Transactional
    public void updateLocation(String token, BigDecimal lat, BigDecimal lng, String locationText) {
        SosEvent event = sosEventRepository.findByPublicTokenAndActiveTrue(token)
                .orElseThrow(() -> new IllegalArgumentException("SOS event not found"));
        event.setLastLat(lat);
        event.setLastLng(lng);
        if (locationText != null) event.setLocationText(locationText);
        event.setUpdatedAt(Instant.now());
        sosEventRepository.save(event);
    }

    // ─────────────────────────────────────────────
    //  Private helpers
    // ─────────────────────────────────────────────

    private SosEvent createOrUpdateSosEvent(User user, SosTriggerRequest request) {
        // Tìm SOS event đang active của user (nếu có → cập nhật)
        return sosEventRepository.findTopByUserIdAndActiveTrueOrderByTriggeredAtDesc(user.getId())
                .map(existing -> {
                    existing.setLastLat(request.getGpsLat());
                    existing.setLastLng(request.getGpsLng());
                    existing.setLocationText(request.getLocationText());
                    existing.setUpdatedAt(Instant.now());
                    existing.setActive(true);
                    existing.setSilent(request.isSilent());
                    existing.setSosType(request.getSosType());
                    return sosEventRepository.save(existing);
                })
                .orElseGet(() -> {
                    String token = generateToken();
                    SosEvent event = SosEvent.builder()
                            .publicToken(token)
                            .user(user)
                            .lastLat(request.getGpsLat())
                            .lastLng(request.getGpsLng())
                            .locationText(request.getLocationText())
                            .triggeredAt(Instant.now())
                            .updatedAt(Instant.now())
                            .active(true)
                            .isSilent(request.isSilent())
                            .sosType(request.getSosType())
                            .build();
                    return sosEventRepository.save(event);
                });
    }

    @Transactional
    public void markAsSafe(String token, String note) {
        SosEvent event = sosEventRepository.findByPublicTokenAndActiveTrue(token)
                .orElseThrow(() -> new IllegalArgumentException("SOS event not found"));
        
        event.setActive(false);
        event.setDeactivatedAt(Instant.now());
        event.setDeactivationNote(note);
        sosEventRepository.save(event);

        // Thông báo cho người thân
        List<EmergencyContact> contacts = contactRepository.findByUserIdOrderByPriorityAsc(event.getUser().getId());
        String msg = "[ANVI SOS] Nạn nhân " + event.getUser().getFullName() + " đã xác nhận AN TOÀN lúc " + VN_FMT.format(Instant.now()) + ".";
        for (EmergencyContact contact : contacts) {
            notificationService.sendToPhone(contact.getPhone(), msg);
        }

        // Broadcast websocket
        var safeMap = new java.util.HashMap<String, Object>();
        safeMap.put("type", "SOS_SAFE");
        safeMap.put("publicToken", token);
        safeMap.put("victimName", event.getUser().getFullName());
        messagingTemplate.convertAndSend("/topic/sos-alerts", safeMap, (java.util.Map<String, Object>) null);
    }

    @Transactional
    public void updateMediaUrl(String token, String mediaUrl) {
        SosEvent event = sosEventRepository.findByPublicTokenAndActiveTrue(token)
                .orElseThrow(() -> new IllegalArgumentException("SOS event not found"));
        event.setMediaUrl(mediaUrl);
        sosEventRepository.save(event);
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String buildAlertUrl(String token) {
        // Trả về URL frontend (port 5173 khi dev)
        return "http://localhost:5173/sos-alert/" + token;
    }

    private void notifyContact(User contactUser, User victim, HealthRecord health, SosTriggerRequest request, String alertUrl, String smsMessage, String publicToken) {
        // 1. Gửi SMS
        notificationService.sendToPhone(contactUser.getPhone(), smsMessage);

        // 2. Gửi Email (nếu có)
        if (contactUser.getEmail() != null && !contactUser.getEmail().isBlank()) {
            double lat = request.getGpsLat() != null ? request.getGpsLat().doubleValue() : 0;
            double lng = request.getGpsLng() != null ? request.getGpsLng().doubleValue() : 0;

            emailService.sendSosAlert(
                    contactUser.getEmail(),
                    victim.getFullName(),
                    victim.getPhone(),
                    lat, lng,
                    health != null ? health.getBloodType() : null,
                    health != null ? health.getAllergies() : null,
                    health != null ? health.getConditions() : null,
                    alertUrl
            );
        }

        // 3. Gửi Real-time WebSocket cho người nhận (Social Connection)
        var contactMap = new java.util.HashMap<String, Object>();
        contactMap.put("type", "SOS_ALERT");
        contactMap.put("victimName", victim.getFullName());
        contactMap.put("publicToken", publicToken);
        contactMap.put("lat", request.getGpsLat() != null ? request.getGpsLat() : 0);
        contactMap.put("lng", request.getGpsLng() != null ? request.getGpsLng() : 0);
        contactMap.put("locationText", request.getLocationText() != null ? request.getLocationText() : "Vị trí không xác định");
        contactMap.put("sosType", request.getSosType() != null ? request.getSosType() : "GENERAL");
        messagingTemplate.convertAndSendToUser(contactUser.getPhone(), "/queue/sos-alerts", contactMap, (java.util.Map<String, Object>) null);
    }

    private String buildInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "??";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (String.valueOf(parts[0].charAt(0)) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private String buildSmsMessage(User user, HealthRecord health, SosTriggerRequest request, String alertUrl) {
        StringBuilder msg = new StringBuilder();
        String time = VN_FMT.format(Instant.now());

        msg.append("[ANVI SOS] ").append(time).append("\n");
        msg.append("NAN NHAN: ").append(user.getFullName());
        msg.append(" | SDT: ").append(user.getPhone()).append("\n");

        if (health != null) {
            if (health.getBloodType() != null) msg.append("Nhom mau: ").append(health.getBloodType()).append("\n");
            if (health.getAllergies() != null) msg.append("Di ung: ").append(health.getAllergies()).append("\n");
        }

        if (request.getGpsLat() != null && request.getGpsLng() != null) {
            String lat = request.getGpsLat().toPlainString();
            String lng = request.getGpsLng().toPlainString();
            msg.append("Chi duong den: https://www.google.com/maps/dir/?api=1&destination=")
               .append(lat).append(",").append(lng).append("&travelmode=driving\n");
        }

        // Link trang alert live
        msg.append("Xem vi tri truc tiep: ").append(alertUrl).append("\n");
        msg.append("HAY DEN NGAY DE HO TRO!");

        return msg.toString();
    }

    /** Inner record để trả về kết quả trigger */
    public record TriggerResult(int sent, String publicToken) {
        public int sentCount() {
            return sent;
        }
    }
}
