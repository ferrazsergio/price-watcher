package com.example.ferrazsergio.pricewatcher.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Common observability configuration
 */
@Configuration
@RequiredArgsConstructor
public class ObservabilityConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "price-watcher");
    }

    @Bean
    public Timer.Builder timerBuilder() {
        return Timer.builder("price.watcher.timer")
                .description("Price watcher operation timer")
                .publishPercentileHistogram();
    }
}