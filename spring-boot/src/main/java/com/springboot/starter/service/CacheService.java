package com.springboot.starter.service;

import com.springboot.starter.model.cache.CacheEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, CacheEvent> kafkaTemplate;

    private static final String CACHE_EVENTS_TOPIC = "cache-events";
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final String CACHE_STATS_PREFIX = "cache_stats:";

    public <T> T getOrSet(String key, Supplier<T> supplier, Duration ttl, String userId) {
        long startTime = System.currentTimeMillis();
        
        try {
            T cachedValue = (T) redisTemplate.opsForValue().get(key);
            
            if (cachedValue != null) {
                long executionTime = System.currentTimeMillis() - startTime;
                publishCacheEvent(CacheEvent.EventType.CACHE_HIT, key, true, executionTime, userId, "Cache hit");
                updateCacheStats(key, true, executionTime);
                log.debug("Cache hit for key: {}, execution time: {}ms", key, executionTime);
                return cachedValue;
            }
            
            // Cache miss - fetch from supplier
            T value = supplier.get();
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Store in cache
            redisTemplate.opsForValue().set(key, value, ttl);
            
            publishCacheEvent(CacheEvent.EventType.CACHE_MISS, key, false, executionTime, userId, "Cache miss - value loaded");
            updateCacheStats(key, false, executionTime);
            log.debug("Cache miss for key: {}, execution time: {}ms", key, executionTime);
            
            return value;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Cache operation failed for key: {}", key, e);
            publishCacheEvent(CacheEvent.EventType.CACHE_MISS, key, false, executionTime, userId, "Cache error: " + e.getMessage());
            return supplier.get();
        }
    }

    public void evict(String key, String userId) {
        long startTime = System.currentTimeMillis();
        
        try {
            redisTemplate.delete(key);
            long executionTime = System.currentTimeMillis() - startTime;
            publishCacheEvent(CacheEvent.EventType.CACHE_EVICT, key, false, executionTime, userId, "Cache evicted");
            log.debug("Cache evicted for key: {}", key);
        } catch (Exception e) {
            log.error("Cache eviction failed for key: {}", key, e);
        }
    }

    public boolean checkRateLimit(String userId, String operation, int maxRequests, Duration window) {
        String rateLimitKey = RATE_LIMIT_PREFIX + userId + ":" + operation;
        long startTime = System.currentTimeMillis();
        
        try {
            String currentCount = (String) redisTemplate.opsForValue().get(rateLimitKey);
            
            if (currentCount == null) {
                redisTemplate.opsForValue().set(rateLimitKey, "1", window);
                publishRateLimitEvent(userId, operation, 1, maxRequests, false, System.currentTimeMillis() - startTime);
                return true;
            }
            
            int count = Integer.parseInt(currentCount);
            if (count >= maxRequests) {
                publishRateLimitEvent(userId, operation, count, maxRequests, true, System.currentTimeMillis() - startTime);
                return false;
            }
            
            redisTemplate.opsForValue().increment(rateLimitKey);
            publishRateLimitEvent(userId, operation, count + 1, maxRequests, false, System.currentTimeMillis() - startTime);
            return true;
            
        } catch (Exception e) {
            log.error("Rate limit check failed for user: {}, operation: {}", userId, operation, e);
            return true; // Fail open
        }
    }

    private void publishCacheEvent(CacheEvent.EventType eventType, String key, boolean hit, long executionTime, String userId, String info) {
        CacheEvent event = CacheEvent.builder()
                .eventType(eventType.name())
                .cacheKey(key)
                .operation(CacheEvent.Operation.READ.name())
                .hit(hit)
                .executionTimeMs(executionTime)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .additionalInfo(info)
                .build();
        
        kafkaTemplate.send(CACHE_EVENTS_TOPIC, key, event);
    }

    private void publishRateLimitEvent(String userId, String operation, int currentCount, int maxRequests, boolean exceeded, long executionTime) {
        CacheEvent event = CacheEvent.builder()
                .eventType(CacheEvent.EventType.RATE_LIMIT_CHECK.name())
                .cacheKey(RATE_LIMIT_PREFIX + userId + ":" + operation)
                .operation(operation)
                .hit(!exceeded)
                .executionTimeMs(executionTime)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .additionalInfo(String.format("Rate limit check: %d/%d requests", currentCount, maxRequests))
                .build();
        
        kafkaTemplate.send(CACHE_EVENTS_TOPIC, userId, event);
    }

    private void updateCacheStats(String key, boolean hit, long executionTime) {
        String statsKey = CACHE_STATS_PREFIX + key;
        
        try {
            redisTemplate.opsForHash().increment(statsKey, hit ? "hits" : "misses", 1);
            redisTemplate.opsForHash().put(statsKey, "lastAccessed", LocalDateTime.now().toString());
            redisTemplate.expire(statsKey, 1, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("Failed to update cache stats for key: {}", key, e);
        }
    }
}
