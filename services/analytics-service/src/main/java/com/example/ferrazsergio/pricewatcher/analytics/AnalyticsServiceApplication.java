package com.example.ferrazsergio.pricewatcher.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {
    "com.example.ferrazsergio.pricewatcher.analytics",
    "com.example.ferrazsergio.pricewatcher.common",
    "com.example.ferrazsergio.pricewatcher.events"
})
@EntityScan("com.example.ferrazsergio.pricewatcher.analytics.model")
@EnableJpaRepositories("com.example.ferrazsergio.pricewatcher.analytics.repository")
@EnableAsync
public class AnalyticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }

}