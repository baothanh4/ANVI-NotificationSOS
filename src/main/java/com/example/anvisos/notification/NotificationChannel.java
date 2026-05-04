package com.example.anvisos.notification;

public interface NotificationChannel {
    void send(String phone, String message);
    String getChannelName();
}

