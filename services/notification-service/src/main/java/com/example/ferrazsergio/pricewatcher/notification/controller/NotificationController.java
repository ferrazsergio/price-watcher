package com.example.ferrazsergio.pricewatcher.notification.controller;

import com.example.ferrazsergio.pricewatcher.notification.model.NotificationData;
import com.example.ferrazsergio.pricewatcher.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST controller for notification operations
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Send a manual notification
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationData notificationData) {
        log.info("Manual notification request for: {}", notificationData.getRecipient());
        
        try {
            notificationService.sendNotification(notificationData);
            return ResponseEntity.ok("Notification sent successfully");
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to send notification: " + e.getMessage());
        }
    }

    /**
     * Send a test email notification
     */
    @PostMapping("/test/email")
    public ResponseEntity<String> sendTestEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String subject = request.getOrDefault("subject", "Test Notification from Price Watcher");
        String message = request.getOrDefault("message", "This is a test notification to verify email functionality.");

        log.info("Sending test email to: {}", email);

        try {
            NotificationData notificationData = NotificationData.builder()
                    .type("EMAIL")
                    .recipient(email)
                    .subject(subject)
                    .message(message)
                    .category("SYSTEM")
                    .priority("LOW")
                    .build();

            notificationService.sendNotification(notificationData);
            return ResponseEntity.ok("Test email sent successfully to " + email);
        } catch (Exception e) {
            log.error("Error sending test email: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to send test email: " + e.getMessage());
        }
    }

    /**
     * Get notification statistics for a user
     */
    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Object>> getNotificationStats(@PathVariable Long userId) {
        try {
            int hourlyCount = notificationService.getNotificationCount(userId, LocalDateTime.now().minusHours(1));
            
            Map<String, Object> stats = Map.of(
                    "userId", userId,
                    "notificationsLastHour", hourlyCount,
                    "timestamp", LocalDateTime.now()
            );

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching notification stats: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running");
    }

    /**
     * Send welcome notification
     */
    @PostMapping("/welcome")
    public ResponseEntity<String> sendWelcomeNotification(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String userName = request.getOrDefault("userName", "New User");

        log.info("Sending welcome notification to: {}", email);

        try {
            notificationService.sendWelcomeNotification(email, userName);
            return ResponseEntity.ok("Welcome notification sent successfully to " + email);
        } catch (Exception e) {
            log.error("Error sending welcome notification: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to send welcome notification: " + e.getMessage());
        }
    }
}