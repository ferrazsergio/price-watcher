package com.example.ferrazsergio.pricewatcher.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {
    "com.example.ferrazsergio.pricewatcher.notification",
    "com.example.ferrazsergio.pricewatcher.common",
    "com.example.ferrazsergio.pricewatcher.events"
})
@EnableAsync
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

}