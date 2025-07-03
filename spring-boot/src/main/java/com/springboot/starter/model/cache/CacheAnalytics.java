package com.springboot.starter.model.cache;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record CacheAnalytics(
    long totalRequests,
    long cacheHits,
    long cacheMisses,
    double hitRate,
    long avgResponseTimeMs,
    Map<String, Long> topCacheKeys,
    Map<String, Long> operationCounts,
    LocalDateTime lastUpdated,
    RateLimitStats rateLimitStats
) {

    @Builder
    public record RateLimitStats(
        long totalRateLimitChecks,
        long rateLimitExceeded,
        Map<String, Long> rateLimitByUser
    ) {
    }
}
