package com.example.ferrazsergio.pricewatcher.common.utils;

import lombok.experimental.UtilityClass;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * Correlation ID utilities for distributed tracing
 */
@UtilityClass
public class CorrelationIdUtils {
    
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    
    public static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
    
    public static void setCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = generateCorrelationId();
        }
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
    }
    
    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID_MDC_KEY);
    }
    
    public static void clearCorrelationId() {
        MDC.remove(CORRELATION_ID_MDC_KEY);
    }
}