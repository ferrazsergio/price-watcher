package com.example.ferrazsergio.pricewatcher.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = {
    "com.example.ferrazsergio.pricewatcher.gateway",
    "com.example.ferrazsergio.pricewatcher.common"
})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service routes
                .route("user-service", r -> r.path("/api/users/**")
                        .filters(f -> f.circuitBreaker(config -> config.setName("user-service-cb")))
                        .uri("http://localhost:8081"))
                
                // Product Service routes
                .route("product-service", r -> r.path("/api/products/**")
                        .filters(f -> f.circuitBreaker(config -> config.setName("product-service-cb")))
                        .uri("http://localhost:8082"))
                
                // Price Monitor Service routes
                .route("price-monitor-service", r -> r.path("/api/price-monitor/**")
                        .filters(f -> f.circuitBreaker(config -> config.setName("price-monitor-service-cb")))
                        .uri("http://localhost:8083"))
                
                // Notification Service routes
                .route("notification-service", r -> r.path("/api/notifications/**")
                        .filters(f -> f.circuitBreaker(config -> config.setName("notification-service-cb")))
                        .uri("http://localhost:8084"))
                
                // Analytics Service routes
                .route("analytics-service", r -> r.path("/api/analytics/**")
                        .filters(f -> f.circuitBreaker(config -> config.setName("analytics-service-cb")))
                        .uri("http://localhost:8085"))
                        
                .build();
    }
}