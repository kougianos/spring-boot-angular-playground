package com.springboot.starter.model.cache;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CacheStats(
    String cacheKey,
    long hitCount,
    long missCount,
    double hitRate,
    long avgResponseTimeMs,
    LocalDateTime lastAccessed,
    LocalDateTime createdAt,
    String dataType
) {}
