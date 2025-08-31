package com.example.ferrazsergio.pricewatcher.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = {
    "com.example.ferrazsergio.pricewatcher.productservice",
    "com.example.ferrazsergio.pricewatcher.common",
    "com.example.ferrazsergio.pricewatcher.events",
    "com.example.ferrazsergio.pricewatcher.security"
})
@EntityScan("com.example.ferrazsergio.pricewatcher.productservice.model")
@EnableJpaRepositories("com.example.ferrazsergio.pricewatcher.productservice.repository")
@EnableCaching
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }

}