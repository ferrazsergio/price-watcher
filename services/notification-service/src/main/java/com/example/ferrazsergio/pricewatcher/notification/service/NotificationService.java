package com.example.ferrazsergio.pricewatcher.notification.service;

import com.example.ferrazsergio.pricewatcher.notification.model.NotificationData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main notification service that handles different notification channels
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${notification.rate-limit.per-user-per-hour:10}")
    private int rateLimitPerUserPerHour;

    /**
     * Send notification through appropriate channel
     */
    @Async
    public void sendNotification(NotificationData notificationData) {
        log.info("Sending notification to {} via {}", 
                notificationData.getRecipient(), notificationData.getType());

        // Check rate limiting
        if (!checkRateLimit(notificationData)) {
            log.warn("Rate limit exceeded for user {}", notificationData.getUserId());
            return;
        }

        try {
            switch (notificationData.getType().toUpperCase()) {
                case "EMAIL":
                    emailService.sendEmail(notificationData);
                    break;
                case "SMS":
                    sendSMS(notificationData);
                    break;
                case "PUSH":
                    sendPushNotification(notificationData);
                    break;
                case "TELEGRAM":
                    sendTelegramMessage(notificationData);
                    break;
                default:
                    log.warn("Unsupported notification type: {}", notificationData.getType());
                    return;
            }

            // Update rate limiting counter
            updateRateLimitCounter(notificationData);
            log.info("Notification sent successfully");

        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send price change notification
     */
    public void sendPriceChangeNotification(NotificationData notificationData) {
        // Generate appropriate subject based on price change
        String subject = generatePriceChangeSubject(notificationData);
        notificationData.setSubject(subject);
        notificationData.setCategory("PRICE_CHANGE");

        sendNotification(notificationData);
    }

    /**
     * Send welcome notification for new users
     */
    public void sendWelcomeNotification(String email, String userName) {
        NotificationData notificationData = NotificationData.builder()
                .type("EMAIL")
                .recipient(email)
                .subject("Bem-vindo ao Price Watcher!")
                .category("WELCOME")
                .priority("NORMAL")
                .build();

        sendNotification(notificationData);
    }

    /**
     * Check if user has exceeded rate limits
     */
    private boolean checkRateLimit(NotificationData notificationData) {
        if (notificationData.getUserId() == null) {
            return true; // No rate limiting for system notifications
        }

        String key = String.format("rate_limit:user:%d:hour:%s", 
                notificationData.getUserId(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH")));

        String currentCount = redisTemplate.opsForValue().get(key);
        int count = currentCount != null ? Integer.parseInt(currentCount) : 0;

        return count < rateLimitPerUserPerHour;
    }

    /**
     * Update rate limiting counter
     */
    private void updateRateLimitCounter(NotificationData notificationData) {
        if (notificationData.getUserId() == null) {
            return; // No rate limiting for system notifications
        }

        String key = String.format("rate_limit:user:%d:hour:%s", 
                notificationData.getUserId(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH")));

        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofHours(1));
    }

    /**
     * Generate subject for price change notifications
     */
    private String generatePriceChangeSubject(NotificationData notificationData) {
        if (notificationData.isPriceTarget()) {
            return String.format("🎉 Meta de preço atingida: %s", notificationData.getProductName());
        } else if (notificationData.isPriceDecrease()) {
            return String.format("📉 Preço reduzido: %s", notificationData.getProductName());
        } else {
            return String.format("📈 Mudança de preço: %s", notificationData.getProductName());
        }
    }

    /**
     * Send SMS notification (placeholder implementation)
     */
    private void sendSMS(NotificationData notificationData) {
        log.info("SMS notification would be sent to: {}", notificationData.getPhoneNumber());
        // TODO: Implement SMS sending via SMS provider (Twilio, etc.)
        log.warn("SMS notifications not yet implemented");
    }

    /**
     * Send push notification (placeholder implementation)
     */
    private void sendPushNotification(NotificationData notificationData) {
        log.info("Push notification would be sent to user: {}", notificationData.getUserId());
        // TODO: Implement push notifications via Firebase Cloud Messaging
        log.warn("Push notifications not yet implemented");
    }

    /**
     * Send Telegram message (placeholder implementation)
     */
    private void sendTelegramMessage(NotificationData notificationData) {
        log.info("Telegram message would be sent to user: {}", notificationData.getUserId());
        // TODO: Implement Telegram Bot API integration
        log.warn("Telegram notifications not yet implemented");
    }

    /**
     * Get notification statistics
     */
    public int getNotificationCount(Long userId, LocalDateTime since) {
        // This could be implemented with a proper logging/metrics system
        // For now, just return the current rate limit count
        String key = String.format("rate_limit:user:%d:hour:%s", 
                userId, since.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH")));
        
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }
}