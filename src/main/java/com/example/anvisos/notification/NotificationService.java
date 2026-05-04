package com.example.anvisos.notification;

import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final List<NotificationChannel> channels;

    public NotificationService(List<NotificationChannel> channels) {
        this.channels = channels;
    }

    public int sendToPhone(String phone, String message) {
        for (NotificationChannel channel : channels) {
            channel.send(phone, message);
        }
        return channels.size();
    }
}

