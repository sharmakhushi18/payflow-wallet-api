package com.payflow.notificationservice.service;

import com.payflow.notificationservice.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(NotificationRequest request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getToEmail());
            message.setSubject(request.getSubject());
            message.setText(request.getMessage());

            mailSender.send(message);
            log.info("Email sent to: {}", request.getToEmail());

        } catch (Exception e) {
            log.error("Email failed to: {} — {}", request.getToEmail(), e.getMessage());
            // Email failure should NOT fail the transaction
        }
    }

    public void sendTransactionEmail(String toEmail, String senderUpi,
                                     String receiverUpi, double amount,
                                     boolean isSender) {
        NotificationRequest request = new NotificationRequest();
        request.setToEmail(toEmail);

        if (isSender) {
            request.setSubject("PayFlow — Money Sent Successfully");
            request.setMessage(
                    "Hi,\n\n" +
                            "Rs." + amount + " has been sent to " + receiverUpi + ".\n\n" +
                            "Transaction details:\n" +
                            "From: " + senderUpi + "\n" +
                            "To: " + receiverUpi + "\n" +
                            "Amount: Rs." + amount + "\n\n" +
                            "Thank you for using PayFlow!"
            );
        } else {
            request.setSubject("PayFlow — Money Received");
            request.setMessage(
                    "Hi,\n\n" +
                            "You have received Rs." + amount + " from " + senderUpi + ".\n\n" +
                            "Transaction details:\n" +
                            "From: " + senderUpi + "\n" +
                            "To: " + receiverUpi + "\n" +
                            "Amount: Rs." + amount + "\n\n" +
                            "Thank you for using PayFlow!"
            );
        }

        sendEmail(request);
    }
}