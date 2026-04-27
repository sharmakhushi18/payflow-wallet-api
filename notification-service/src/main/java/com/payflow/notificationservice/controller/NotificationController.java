package com.payflow.notificationservice.controller;

import com.payflow.notificationservice.dto.NotificationRequest;
import com.payflow.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(
            @RequestBody NotificationRequest request) {
        emailService.sendEmail(request);
        return ResponseEntity.ok("Email sent successfully");
    }

    @PostMapping("/transaction")
    public ResponseEntity<String> sendTransactionEmail(
            @RequestParam String toEmail,
            @RequestParam String senderUpi,
            @RequestParam String receiverUpi,
            @RequestParam double amount,
            @RequestParam boolean isSender) {

        emailService.sendTransactionEmail(
                toEmail, senderUpi, receiverUpi, amount, isSender);
        return ResponseEntity.ok("Transaction email sent");
    }
}