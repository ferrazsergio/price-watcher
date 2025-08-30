package com.example.ferrazsergio.pricewatcher.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.example.ferrazsergio.pricewatcher.userservice",
    "com.example.ferrazsergio.pricewatcher.common",
    "com.example.ferrazsergio.pricewatcher.events"
})
@EntityScan("com.example.ferrazsergio.pricewatcher.userservice.model")
@EnableJpaRepositories("com.example.ferrazsergio.pricewatcher.userservice.repository")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

}