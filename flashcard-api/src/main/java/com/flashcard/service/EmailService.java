package com.flashcard.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:noreply@flashcardapp.com}")
    private String senderEmail;

    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset OTP - Mobile Flashcard App");

            String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>"
                    + "<h2 style='color: #4CAF50; text-align: center;'>Mobile Flashcard App</h2>"
                    + "<p>Hello,</p>"
                    + "<p>You recently requested to reset your password. Use the following OTP to complete the process. This OTP is valid for <strong>5 minutes</strong>.</p>"
                    + "<div style='text-align: center; margin: 20px 0;'>"
                    + "<span style='font-size: 24px; font-weight: bold; background-color: #f4f4f4; padding: 10px 20px; border-radius: 5px; letter-spacing: 5px;'>" 
                    + otpCode + "</span>"
                    + "</div>"
                    + "<p>If you did not request a password reset, please ignore this email.</p>"
                    + "<p>Best Regards,<br/>Flashcard Team</p>"
                    + "</div>";

            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("OTP email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
}
