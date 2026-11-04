package com.fundspark.fundspark_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class PasswordResetEmailService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetEmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.password-reset.email-from:noreply@fundspark.local}")
    private String fromAddress;

    public PasswordResetEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("FundSpark password reset code");
            helper.setText(
                    """
                    You requested a password reset for your FundSpark account.

                    Your verification code is: %s

                    This code expires in 5 minutes. If you did not request this, you can ignore this email.
                    """.formatted(otp),
                    false
            );
            mailSender.send(message);
            log.info("Password reset OTP sent to {}", to);
        } catch (MailException | MessagingException ex) {
            log.error("Failed to send password reset OTP to {}", to, ex);
            throw new IllegalStateException("Unable to send password reset email right now.", ex);
        }
    }
}
