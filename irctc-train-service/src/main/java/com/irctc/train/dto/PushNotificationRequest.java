package com.irctc.train.dto;

import lombok.Data;

import java.util.Map;

@Data
public class PushNotificationRequest {
    private Long userId;
    private String title;
    private String body;
    private String notificationType;
    private Map<String, String> data;
}

