package com.springboot.starter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.starter.model.cache.CacheAnalytics;
import com.springboot.starter.model.cache.CacheEvent;
import com.springboot.starter.model.cache.CacheStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheAnalyticsService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ANALYTICS_PREFIX = "analytics:";
    private static final String CACHE_STATS_PREFIX = "cache_stats:";
    private static final String GLOBAL_STATS_KEY = ANALYTICS_PREFIX + "global";
    private final ObjectMapper mapper;

    @KafkaListener(topics = "cache-events", groupId = "cache-analytics-group")
    public void handleCacheEvent(String payload) throws JsonProcessingException {
        var event = mapper.readValue(payload, CacheEvent.class);
        try {
            updateGlobalStats(event);
            log.debug("Processed cache event: {}", event.eventType());
        } catch (Exception e) {
            log.error("Failed to process cache event: {}", event, e);
        }
    }

    public CacheAnalytics getGlobalAnalytics() {
        try {
            Map<Object, Object> globalStats = redisTemplate.opsForHash().entries(GLOBAL_STATS_KEY);
            
            if (globalStats.isEmpty()) {
                return createEmptyAnalytics();
            }
            
            long totalRequests = getLongValue(globalStats, "totalRequests", 0L);
            long cacheHits = getLongValue(globalStats, "cacheHits", 0L);
            long cacheMisses = getLongValue(globalStats, "cacheMisses", 0L);
            long totalResponseTime = getLongValue(globalStats, "totalResponseTime", 0L);
            long rateLimitChecks = getLongValue(globalStats, "rateLimitChecks", 0L);
            long rateLimitExceeded = getLongValue(globalStats, "rateLimitExceeded", 0L);
            
            double hitRate = totalRequests > 0 ? (double) cacheHits / totalRequests * 100 : 0.0;
            long avgResponseTime = totalRequests > 0 ? totalResponseTime / totalRequests : 0L;
            
            return CacheAnalytics.builder()
                    .totalRequests(totalRequests)
                    .cacheHits(cacheHits)
                    .cacheMisses(cacheMisses)
                    .hitRate(hitRate)
                    .avgResponseTimeMs(avgResponseTime)
                    .topCacheKeys(getTopCacheKeys())
                    .operationCounts(getOperationCounts())
                    .lastUpdated(LocalDateTime.now())
                    .rateLimitStats(CacheAnalytics.RateLimitStats.builder()
                            .totalRateLimitChecks(rateLimitChecks)
                            .rateLimitExceeded(rateLimitExceeded)
                            .rateLimitByUser(getRateLimitByUser())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Failed to get global analytics", e);
            return createEmptyAnalytics();
        }
    }

    public List<CacheStats> getAllCacheStats() {
        try {
            Set<String> keys = redisTemplate.keys(CACHE_STATS_PREFIX + "*");
            if (keys.isEmpty()) {
                return List.of();
            }
            
            return keys.stream()
                    .map(this::getCacheStatsForKey)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to get all cache stats", e);
            return List.of();
        }
    }

    public void clearAnalytics() {
        try {
            Set<String> analyticsKeys = redisTemplate.keys(ANALYTICS_PREFIX + "*");
            Set<String> statsKeys = redisTemplate.keys(CACHE_STATS_PREFIX + "*");
            
            if (!analyticsKeys.isEmpty()) {
                redisTemplate.delete(analyticsKeys);
            }
            if (!statsKeys.isEmpty()) {
                redisTemplate.delete(statsKeys);
            }
            
            log.info("Cleared all analytics data");
        } catch (Exception e) {
            log.error("Failed to clear analytics", e);
        }
    }

    private void updateGlobalStats(CacheEvent event) {
        String statsKey = GLOBAL_STATS_KEY;
        
        // Update total requests
        redisTemplate.opsForHash().increment(statsKey, "totalRequests", 1);
        
        // Update cache hits/misses
        if (event.hit()) {
            redisTemplate.opsForHash().increment(statsKey, "cacheHits", 1);
        } else {
            redisTemplate.opsForHash().increment(statsKey, "cacheMisses", 1);
        }
        
        // Update response time
        redisTemplate.opsForHash().increment(statsKey, "totalResponseTime", event.executionTimeMs());
        
        // Update rate limit stats
        if ("RATE_LIMIT_CHECK".equals(event.eventType())) {
            redisTemplate.opsForHash().increment(statsKey, "rateLimitChecks", 1);
            if (!event.hit()) {
                redisTemplate.opsForHash().increment(statsKey, "rateLimitExceeded", 1);
            }
        }
        
        // Update top cache keys
        if (event.cacheKey() != null) {
            redisTemplate.opsForHash().increment(ANALYTICS_PREFIX + "topKeys", event.cacheKey(), 1);
        }
        
        // Update operation counts
        if (event.operation() != null) {
            redisTemplate.opsForHash().increment(ANALYTICS_PREFIX + "operations", event.operation(), 1);
        }
        
        // Update rate limit by user
        if (event.userId() != null && "RATE_LIMIT_CHECK".equals(event.eventType())) {
            redisTemplate.opsForHash().increment(ANALYTICS_PREFIX + "rateLimitUsers", event.userId(), 1);
        }
        
        redisTemplate.opsForHash().put(statsKey, "lastUpdated", LocalDateTime.now().toString());
    }

    private CacheStats getCacheStatsForKey(String fullKey) {
        String cacheKey = fullKey.replace(CACHE_STATS_PREFIX, "");
        Map<Object, Object> stats = redisTemplate.opsForHash().entries(fullKey);
        
        long hits = getLongValue(stats, "hits", 0L);
        long misses = getLongValue(stats, "misses", 0L);
        long total = hits + misses;
        double hitRate = total > 0 ? (double) hits / total * 100 : 0.0;
        
        return CacheStats.builder()
                .cacheKey(cacheKey)
                .hitCount(hits)
                .missCount(misses)
                .hitRate(hitRate)
                .avgResponseTimeMs(0L) // Could be calculated if needed
                .lastAccessed(LocalDateTime.parse(stats.getOrDefault("lastAccessed", LocalDateTime.now().toString()).toString()))
                .createdAt(LocalDateTime.now())
                .dataType("Unknown")
                .build();
    }

    private Map<String, Long> getTopCacheKeys() {
        Map<Object, Object> topKeys = redisTemplate.opsForHash().entries(ANALYTICS_PREFIX + "topKeys");
        return topKeys.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        entry -> Long.parseLong(entry.getValue().toString())
                ));
    }

    private Map<String, Long> getOperationCounts() {
        Map<Object, Object> operations = redisTemplate.opsForHash().entries(ANALYTICS_PREFIX + "operations");
        return operations.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        entry -> Long.parseLong(entry.getValue().toString())
                ));
    }

    private Map<String, Long> getRateLimitByUser() {
        Map<Object, Object> rateLimitUsers = redisTemplate.opsForHash().entries(ANALYTICS_PREFIX + "rateLimitUsers");
        return rateLimitUsers.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        entry -> Long.parseLong(entry.getValue().toString())
                ));
    }

    private CacheAnalytics createEmptyAnalytics() {
        return CacheAnalytics.builder()
                .totalRequests(0L)
                .cacheHits(0L)
                .cacheMisses(0L)
                .hitRate(0.0)
                .avgResponseTimeMs(0L)
                .topCacheKeys(new HashMap<>())
                .operationCounts(new HashMap<>())
                .lastUpdated(LocalDateTime.now())
                .rateLimitStats(CacheAnalytics.RateLimitStats.builder()
                        .totalRateLimitChecks(0L)
                        .rateLimitExceeded(0L)
                        .rateLimitByUser(new HashMap<>())
                        .build())
                .build();
    }

    private Long getLongValue(Map<Object, Object> map, String key, Long defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        return Long.parseLong(value.toString());
    }
}
