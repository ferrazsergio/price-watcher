package com.example.ferrazsergio.pricewatcher.events.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for event-driven architecture
 */
@Configuration
@EnableRabbit
@RequiredArgsConstructor
public class RabbitMQConfig {

    // Exchange names
    public static final String PRICE_WATCHER_EXCHANGE = "price-watcher-exchange";
    
    // Queue names
    public static final String PRICE_CHANGE_QUEUE = "price-change-queue";
    public static final String USER_EVENTS_QUEUE = "user-events-queue";
    public static final String NOTIFICATION_QUEUE = "notification-queue";
    public static final String ANALYTICS_QUEUE = "analytics-queue";
    
    // Routing keys
    public static final String PRICE_CHANGE_ROUTING_KEY = "price.change.detected";
    public static final String USER_CREATED_ROUTING_KEY = "user.created";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.send";
    public static final String ANALYTICS_ROUTING_KEY = "analytics.process";

    @Bean
    public TopicExchange priceWatcherExchange() {
        return new TopicExchange(PRICE_WATCHER_EXCHANGE);
    }

    @Bean
    public Queue priceChangeQueue() {
        return QueueBuilder.durable(PRICE_CHANGE_QUEUE).build();
    }

    @Bean
    public Queue userEventsQueue() {
        return QueueBuilder.durable(USER_EVENTS_QUEUE).build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Queue analyticsQueue() {
        return QueueBuilder.durable(ANALYTICS_QUEUE).build();
    }

    @Bean
    public Binding priceChangeBinding() {
        return BindingBuilder.bind(priceChangeQueue())
                .to(priceWatcherExchange())
                .with(PRICE_CHANGE_ROUTING_KEY);
    }

    @Bean
    public Binding userCreatedBinding() {
        return BindingBuilder.bind(userEventsQueue())
                .to(priceWatcherExchange())
                .with(USER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(priceWatcherExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Binding analyticsBinding() {
        return BindingBuilder.bind(analyticsQueue())
                .to(priceWatcherExchange())
                .with(ANALYTICS_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}