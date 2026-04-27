package com.payflow.notificationservice.dto;

import lombok.Data;

@Data
public class NotificationRequest {
    private String toEmail;
    private String subject;
    private String message;
}