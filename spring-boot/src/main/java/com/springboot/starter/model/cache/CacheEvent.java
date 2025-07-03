package com.springboot.starter.model.cache;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CacheEvent(
    String eventType,
    String cacheKey,
    String operation,
    boolean hit,
    long executionTimeMs,
    String userId,
    LocalDateTime timestamp,
    String additionalInfo
) {
    
    public enum EventType {
        CACHE_HIT,
        CACHE_MISS,
        CACHE_PUT,
        CACHE_EVICT,
        RATE_LIMIT_CHECK,
        API_CALL
    }
    
    public enum Operation {
        READ,
        WRITE,
        DELETE
    }
}
