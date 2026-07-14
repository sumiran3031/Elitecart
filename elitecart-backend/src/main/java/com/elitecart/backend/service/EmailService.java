package com.elitecart.backend.service;

import com.elitecart.backend.entity.User;

public interface EmailService {
    void sendVerificationEmail(User user, String verificationLink);
    void sendForgotPasswordEmail(User user, String resetLink);
    void sendOrderConfirmationEmail(User user, String orderNumber, String orderSummaryHtml);
    void sendOrderShippedEmail(User user, String orderNumber);
    void sendOrderDeliveredEmail(User user, String orderNumber);
}
