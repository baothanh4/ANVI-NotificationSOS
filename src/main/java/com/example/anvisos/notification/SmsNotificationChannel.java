package com.example.anvisos.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class SmsNotificationChannel implements NotificationChannel {
    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationChannel.class);

    @Value("${esms.api-key}")
    private String apiKey;

    @Value("${esms.api-secret}")
    private String apiSecret;

    @Value("${esms.brand-name}")
    private String brandName;

    @Value("${esms.enabled:false}")
    private boolean enabled;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void send(String phone, String message) {
        if (!enabled) {
            logger.info("[SMS-STUB] to={} | message={}", phone, message);
            return;
        }

        try {
            // Chuan hoa so dien thoai VN: 0xxx -> 84xxx
            String normalizedPhone = normalizePhone(phone);

            String url = "https://rest.esms.vn/MainService.svc/json/SendMultipleMessage_V4_post_json/";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("ApiKey", apiKey);
            body.put("ApiSecret", apiSecret);
            body.put("Brandname", brandName);
            body.put("SmsType", "2"); // 2 = Brandname OTP/CSKH
            body.put("Phone", normalizedPhone);
            body.put("Content", message);
            body.put("IsUnicode", "0"); // 0 = ASCII, 1 = Unicode (co dau)

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            logger.info("[SMS] Sent to={} | ESMS response: {}", phone, response.getBody());
        } catch (Exception e) {
            logger.error("[SMS] Failed to send to={} | error={}", phone, e.getMessage());
        }
    }

    private String normalizePhone(String phone) {
        if (phone == null) return phone;
        phone = phone.trim().replaceAll("\\s+", "");
        if (phone.startsWith("0")) {
            return "84" + phone.substring(1);
        }
        if (phone.startsWith("+84")) {
            return phone.substring(1); // bo dau +
        }
        return phone;
    }

    @Override
    public String getChannelName() {
        return "SMS";
    }
}
