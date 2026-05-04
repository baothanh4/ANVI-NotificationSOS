package com.example.anvisos.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Kênh thông báo Email — tích hợp vào NotificationService giống SMS.
 * Dùng để gửi plain-text thông báo qua email (fallback).
 * Để gửi SOS với HTML đầy đủ, dùng EmailService.sendSosAlert() trực tiếp.
 */
@Component
public class EmailNotificationChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationChannel.class);

    @Value("${anvi.email.notification.enabled:true}")
    private boolean enabled;

    private final EmailService emailService;

    public EmailNotificationChannel(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Gửi thông báo SOS qua email.
     * Ở đây "phone" được tái sử dụng để truyền địa chỉ email của emergency contact.
     */
    @Override
    public void send(String emailAddress, String message) {
        if (!enabled) {
            log.info("[EMAIL-STUB] to={} | message={}", emailAddress, message);
            return;
        }
        if (emailAddress == null || !emailAddress.contains("@")) {
            log.warn("[EMAIL] Invalid email address: {}, skipping", emailAddress);
            return;
        }
        // Gửi dưới dạng SOS alert đơn giản (plain text trong HTML wrapper)
        emailService.sendSosAlert(
                emailAddress,
                extractField(message, "Nạn nhân:"),
                extractField(message, "SĐT:"),
                0.0, 0.0,
                extractField(message, "Nhóm máu:"),
                extractField(message, "Dị ứng:"),
                extractField(message, "Bệnh:"),
                null
        );
    }

    @Override
    public String getChannelName() {
        return "EMAIL";
    }

    /** Hàm tiện ích: trích xuất giá trị từ chuỗi tin nhắn dạng "Key: value" */
    private String extractField(String message, String key) {
        if (message == null) return null;
        int idx = message.indexOf(key);
        if (idx < 0) return null;
        int start = idx + key.length();
        int end = message.indexOf("\n", start);
        String value = (end < 0 ? message.substring(start) : message.substring(start, end)).trim();
        return value.isEmpty() ? null : value;
    }
}
