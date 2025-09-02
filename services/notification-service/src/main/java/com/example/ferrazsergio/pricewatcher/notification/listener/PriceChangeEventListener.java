package com.example.ferrazsergio.pricewatcher.notification.listener;

import com.example.ferrazsergio.pricewatcher.events.model.PriceChangeDetectedEvent;
import com.example.ferrazsergio.pricewatcher.notification.model.NotificationData;
import com.example.ferrazsergio.pricewatcher.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * RabbitMQ listener for price change events
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PriceChangeEventListener {

    private final NotificationService notificationService;
    private final WebClient webClient;

    private static final String USER_SERVICE_URL = "http://localhost:8081";

    /**
     * Listen for price change events and send notifications
     */
    @RabbitListener(queues = "notification-queue")
    public void handlePriceChangeEvent(PriceChangeDetectedEvent event) {
        log.info("Received price change event for product: {}", event.getProductId());

        try {
            // Fetch user details to get email and notification preferences
            UserInfo userInfo = fetchUserInfo(event.getUserId());
            
            if (userInfo == null) {
                log.warn("User not found for ID: {}", event.getUserId());
                return;
            }

            // Check if user wants notifications for this type of change
            if (!shouldSendNotification(userInfo, event)) {
                log.debug("User {} has disabled notifications for this type of price change", event.getUserId());
                return;
            }

            // Create notification data
            NotificationData notificationData = createNotificationData(event, userInfo);

            // Send notification
            notificationService.sendPriceChangeNotification(notificationData);

        } catch (Exception e) {
            log.error("Error processing price change event for product {}: {}", 
                    event.getProductId(), e.getMessage(), e);
        }
    }

    /**
     * Fetch user information from user service
     */
    private UserInfo fetchUserInfo(Long userId) {
        try {
            Mono<Map> response = webClient.get()
                    .uri(USER_SERVICE_URL + "/api/users/" + userId)
                    .retrieve()
                    .bodyToMono(Map.class);

            Map<String, Object> userMap = response.block();
            
            if (userMap == null) {
                return null;
            }

            return UserInfo.builder()
                    .id(((Number) userMap.get("id")).longValue())
                    .email((String) userMap.get("email"))
                    .firstName((String) userMap.get("firstName"))
                    .lastName((String) userMap.get("lastName"))
                    .phoneNumber((String) userMap.get("phoneNumber"))
                    .emailNotifications((Boolean) userMap.getOrDefault("emailNotifications", true))
                    .smsNotifications((Boolean) userMap.getOrDefault("smsNotifications", false))
                    .pushNotifications((Boolean) userMap.getOrDefault("pushNotifications", false))
                    .build();

        } catch (Exception e) {
            log.error("Error fetching user info for user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * Check if notification should be sent based on user preferences
     */
    private boolean shouldSendNotification(UserInfo userInfo, PriceChangeDetectedEvent event) {
        // For now, only check email notifications
        // This could be expanded to check specific preferences for:
        // - Price increases
        // - Price decreases
        // - Target price achieved
        // - Specific product categories
        return userInfo.isEmailNotifications();
    }

    /**
     * Create notification data from price change event
     */
    private NotificationData createNotificationData(PriceChangeDetectedEvent event, UserInfo userInfo) {
        return NotificationData.builder()
                .type("EMAIL")
                .recipient(userInfo.getEmail())
                .productId(event.getProductId())
                .productName(event.getProductName())
                .productUrl(event.getProductUrl())
                .oldPrice(event.getPreviousPrice())
                .newPrice(event.getCurrentPrice())
                .targetPrice(event.getTargetPrice())
                .userId(event.getUserId())
                .userEmail(userInfo.getEmail())
                .phoneNumber(userInfo.getPhoneNumber())
                .category("PRICE_CHANGE")
                .priority(determinePriority(event))
                .build();
    }

    /**
     * Determine notification priority based on price change
     */
    private String determinePriority(PriceChangeDetectedEvent event) {
        // Target price achieved = HIGH priority
        if (event.getTargetPrice() != null && 
            event.getCurrentPrice().compareTo(event.getTargetPrice()) <= 0) {
            return "HIGH";
        }
        
        // Significant price decrease = NORMAL priority
        if (event.getPreviousPrice() != null && 
            event.getCurrentPrice().compareTo(event.getPreviousPrice()) < 0) {
            return "NORMAL";
        }
        
        // Price increase = LOW priority
        return "LOW";
    }

    /**
     * Inner class for user information
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class UserInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private boolean emailNotifications;
        private boolean smsNotifications;
        private boolean pushNotifications;
    }
}