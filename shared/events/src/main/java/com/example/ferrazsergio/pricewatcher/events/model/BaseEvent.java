package com.example.ferrazsergio.pricewatcher.events.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Base event class for all domain events
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {
    
    private String eventId;
    private String eventType;
    private String correlationId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    protected BaseEvent(String eventType) {
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
        this.eventId = java.util.UUID.randomUUID().toString();
    }
}