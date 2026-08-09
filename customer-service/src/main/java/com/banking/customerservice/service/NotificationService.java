package com.banking.customerservice.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    @Async
    public void sendProfileCreatedEmail(String recipientEmail, String customerName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject("Your customer profile was created successfully");
            message.setText("Hello " + customerName + ",\n\nYour customer profile has been created successfully. "
                    + "You can now proceed to create bank or community accounts.\n\nRegards,\nMbhombi Financial Services");
            mailSender.send(message);
            log.info("Profile creation email sent to {}", recipientEmail);
        } catch (Exception ex) {
            log.warn("Unable to send profile creation email to {}. Cause: {}", recipientEmail, ex.getMessage());
        }
    }
}
