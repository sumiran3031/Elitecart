package com.elitecart.backend.service.impl;

import com.elitecart.backend.entity.EmailLog;
import com.elitecart.backend.entity.User;
import com.elitecart.backend.repository.EmailLogRepository;
import com.elitecart.backend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Sends transactional emails using Spring Mail with beautiful HTML templates,
 * and records every attempt (success or failure) in the email_logs table for auditing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    @Async
    public void sendVerificationEmail(User user, String verificationLink) {
        String subject = "Welcome to EliteCart - Verify Your Email";
        String html = wrapTemplate(
                "Welcome to EliteCart, " + user.getFirstName() + "!",
                "Thanks for signing up. Please verify your email address to activate your account.",
                verificationLink,
                "Verify Email"
        );
        send(user.getEmail(), subject, html, EmailLog.EmailType.REGISTRATION_VERIFICATION);
    }

    @Override
    @Async
    public void sendForgotPasswordEmail(User user, String resetLink) {
        String subject = "EliteCart - Reset Your Password";
        String html = wrapTemplate(
                "Password Reset Requested",
                "We received a request to reset your password. This link expires in 30 minutes. If you didn't request this, please ignore this email.",
                resetLink,
                "Reset Password"
        );
        send(user.getEmail(), subject, html, EmailLog.EmailType.FORGOT_PASSWORD);
    }

    @Override
    @Async
    public void sendOrderConfirmationEmail(User user, String orderNumber, String orderSummaryHtml) {
        String subject = "EliteCart - Order Confirmed #" + orderNumber;
        String html = wrapTemplate(
                "Your Order is Confirmed!",
                "Thank you for shopping with EliteCart. Order <strong>" + orderNumber + "</strong> has been placed successfully."
                        + "<br/><br/>" + orderSummaryHtml,
                null,
                null
        );
        send(user.getEmail(), subject, html, EmailLog.EmailType.ORDER_CONFIRMATION);
    }

    @Override
    @Async
    public void sendOrderShippedEmail(User user, String orderNumber) {
        String subject = "EliteCart - Order Shipped #" + orderNumber;
        String html = wrapTemplate(
                "Your Order Has Shipped!",
                "Good news! Order <strong>" + orderNumber + "</strong> is on its way to you.",
                null,
                null
        );
        send(user.getEmail(), subject, html, EmailLog.EmailType.ORDER_SHIPPED);
    }

    @Override
    @Async
    public void sendOrderDeliveredEmail(User user, String orderNumber) {
        String subject = "EliteCart - Order Delivered #" + orderNumber;
        String html = wrapTemplate(
                "Your Order Has Been Delivered!",
                "Order <strong>" + orderNumber + "</strong> has been delivered. We hope you love it!",
                null,
                null
        );
        send(user.getEmail(), subject, html, EmailLog.EmailType.ORDER_DELIVERED);
    }

    private void send(String to, String subject, String html, EmailLog.EmailType type) {
        EmailLog.EmailLogBuilder logBuilder = EmailLog.builder()
                .recipient(to)
                .subject(subject)
                .emailType(type);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);

            emailLogRepository.save(logBuilder.status(EmailLog.Status.SENT).build());
        } catch (MessagingException | RuntimeException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            emailLogRepository.save(logBuilder.status(EmailLog.Status.FAILED).errorMessage(e.getMessage()).build());
        }
    }

    private String wrapTemplate(String heading, String bodyText, String actionUrl, String actionLabel) {
        String button = (actionUrl != null)
                ? "<div style='text-align:center;margin:32px 0;'>"
                + "<a href='" + actionUrl + "' style='background:linear-gradient(135deg,#6366f1,#8b5cf6);color:#ffffff;"
                + "padding:14px 32px;border-radius:8px;text-decoration:none;font-weight:600;display:inline-block;'>"
                + actionLabel + "</a></div>"
                : "";

        return "<!DOCTYPE html><html><body style='margin:0;padding:0;background:#f4f4f7;font-family:Segoe UI,Arial,sans-serif;'>"
                + "<div style='max-width:560px;margin:40px auto;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);'>"
                + "<div style='background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:32px;text-align:center;'>"
                + "<h1 style='color:#ffffff;margin:0;font-size:24px;'>EliteCart</h1></div>"
                + "<div style='padding:32px;'>"
                + "<h2 style='color:#1f2937;font-size:20px;margin-top:0;'>" + heading + "</h2>"
                + "<p style='color:#4b5563;line-height:1.6;font-size:15px;'>" + bodyText + "</p>"
                + button
                + "<p style='color:#9ca3af;font-size:12px;margin-top:32px;'>If you did not expect this email, you can safely ignore it.</p>"
                + "</div>"
                + "<div style='background:#f9fafb;padding:16px;text-align:center;color:#9ca3af;font-size:12px;'>"
                + "&copy; 2026 EliteCart. All rights reserved.</div>"
                + "</div></body></html>";
    }
}
