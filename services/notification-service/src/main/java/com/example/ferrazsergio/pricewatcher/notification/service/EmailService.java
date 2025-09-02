package com.example.ferrazsergio.pricewatcher.notification.service;

import com.example.ferrazsergio.pricewatcher.notification.model.NotificationData;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Service for sending email notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${notification.email.from:noreply@pricewatcher.com}")
    private String fromEmail;

    @Value("${notification.email.from-name:Price Watcher}")
    private String fromName;

    /**
     * Send email notification
     */
    public void sendEmail(NotificationData notificationData) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(notificationData.getRecipient());
            helper.setSubject(notificationData.getSubject());

            String htmlContent = generateEmailContent(notificationData);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", notificationData.getRecipient());

        } catch (MessagingException | MailException e) {
            log.error("Failed to send email to {}: {}", notificationData.getRecipient(), e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}: {}", notificationData.getRecipient(), e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Generate email content using Thymeleaf templates
     */
    private String generateEmailContent(NotificationData notificationData) {
        Context context = new Context();
        
        // Common variables
        context.setVariable("productName", notificationData.getProductName());
        context.setVariable("productUrl", notificationData.getProductUrl());
        
        // Price formatting
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        
        if (notificationData.getOldPrice() != null) {
            context.setVariable("oldPrice", currencyFormat.format(notificationData.getOldPrice()));
        }
        
        if (notificationData.getNewPrice() != null) {
            context.setVariable("newPrice", currencyFormat.format(notificationData.getNewPrice()));
        }
        
        if (notificationData.getTargetPrice() != null) {
            context.setVariable("targetPrice", currencyFormat.format(notificationData.getTargetPrice()));
        }

        // Price change analysis
        context.setVariable("isPriceTarget", notificationData.isPriceTarget());
        context.setVariable("isPriceIncrease", notificationData.isPriceIncrease());
        context.setVariable("isPriceDecrease", notificationData.isPriceDecrease());
        
        // Calculate savings if price target is achieved
        if (notificationData.isPriceTarget() && notificationData.getTargetPrice() != null) {
            BigDecimal savings = notificationData.getTargetPrice().subtract(notificationData.getNewPrice());
            context.setVariable("savings", currencyFormat.format(savings));
        }

        // Additional context
        context.setVariable("timestamp", notificationData.getCreatedAt());
        context.setVariable("category", notificationData.getCategory());

        // Determine template based on notification type
        String templateName = getTemplateName(notificationData);
        
        return templateEngine.process(templateName, context);
    }

    /**
     * Determine template name based on notification data
     */
    private String getTemplateName(NotificationData notificationData) {
        switch (notificationData.getCategory().toUpperCase()) {
            case "PRICE_CHANGE":
                if (notificationData.isPriceTarget()) {
                    return "email/price-target-achieved";
                } else if (notificationData.isPriceDecrease()) {
                    return "email/price-decrease";
                } else {
                    return "email/price-increase";
                }
            case "WELCOME":
                return "email/welcome";
            case "SYSTEM":
                return "email/system-notification";
            default:
                return "email/generic-notification";
        }
    }

    /**
     * Send simple text email (fallback)
     */
    public void sendSimpleEmail(String to, String subject, String message) {
        NotificationData notificationData = NotificationData.builder()
                .recipient(to)
                .subject(subject)
                .message(message)
                .category("SYSTEM")
                .build();
        
        sendEmail(notificationData);
    }
}