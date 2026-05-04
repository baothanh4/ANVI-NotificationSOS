package com.example.anvisos.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ZaloNotificationChannel implements NotificationChannel {
    private static final Logger logger = LoggerFactory.getLogger(ZaloNotificationChannel.class);

    @Override
    public void send(String phone, String message) {
        logger.info("[ZALO] to={} message={}", phone, message);
    }

    @Override
    public String getChannelName() {
        return "ZALO";
    }
}

