package com.example.ferrazsergio.pricewatcher.pricemonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
    "com.example.ferrazsergio.pricewatcher.pricemonitor",
    "com.example.ferrazsergio.pricewatcher.common",
    "com.example.ferrazsergio.pricewatcher.events"
})
@EntityScan("com.example.ferrazsergio.pricewatcher.pricemonitor.model")
@EnableJpaRepositories("com.example.ferrazsergio.pricewatcher.pricemonitor.repository")
@EnableScheduling
@EnableAsync
@EnableRetry
public class PriceMonitorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PriceMonitorServiceApplication.class, args);
    }

}