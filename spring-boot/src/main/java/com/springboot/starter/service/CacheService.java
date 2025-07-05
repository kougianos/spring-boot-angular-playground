package com.springboot.starter.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    private static final String CACHE_EVENTS_TOPIC = "cache-events";
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final String CACHE_STATS_PREFIX = "cache_stats:";

    public <T> T getOrSet(String key, Supplier<T> supplier, Duration ttl, String userId, Class<T> type) {
        return getOrSetInternal(key, supplier, ttl, userId, cachedValue -> {
            if (type.isInstance(cachedValue)) {
                return type.cast(cachedValue);
            }
            return objectMapper.convertValue(cachedValue, type);
        });
    }

    public <T> T getOrSet(String key, Supplier<T> supplier, Duration ttl, String userId,
                          TypeReference<T> typeReference) {
        return getOrSetInternal(key, supplier, ttl, userId,
            cachedValue -> objectMapper.convertValue(cachedValue, typeReference));
    }

    private <T> T getOrSetInternal(String key, Supplier<T> supplier, Duration ttl, String userId,
                                   java.util.function.Function<Object, T> valueConverter) {
        long startTime = System.currentTimeMillis();
        try {
            Object cachedValue = redisTemplate.opsForValue().get(key);
            long executionTime = System.currentTimeMillis() - startTime;

            if (cachedValue != null) {
                publishCacheEvent(CacheEvent.EventType.CACHE_HIT, key, true, executionTime, userId, "Cache hit");
                updateCacheStats(key, true, executionTime);
                log.debug("Cache hit for key: {}, execution time: {}ms", key, executionTime);
                return valueConverter.apply(cachedValue);
            }

            T value = supplier.get();
            redisTemplate.opsForValue().set(key, value, ttl);

            publishCacheEvent(CacheEvent.EventType.CACHE_MISS, key, false, executionTime, userId,
                "Cache miss - value loaded");
            updateCacheStats(key, false, executionTime);
            log.debug("Cache miss for key: {}, execution time: {}ms", key, executionTime);

            return value;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Cache operation failed for key: {}", key, e);
            publishCacheEvent(CacheEvent.EventType.CACHE_MISS, key, false, executionTime, userId,
                "Cache error: " + e.getMessage());
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
            Long currentCount = redisTemplate.opsForValue().increment(rateLimitKey);

            assert currentCount != null;
            if (currentCount.intValue() == 1) {
                redisTemplate.expire(rateLimitKey, window);
            }

            if (currentCount > maxRequests) {
                publishRateLimitEvent(userId, operation, currentCount.intValue(), maxRequests, true,
                    System.currentTimeMillis() - startTime);
                return false;
            }

            publishRateLimitEvent(userId, operation, currentCount.intValue(), maxRequests, false,
                System.currentTimeMillis() - startTime);
            return true;

        } catch (Exception e) {
            log.error("Rate limit check failed for user: {}, operation: {}", userId, operation, e);
            return true; // Fail open
        }
    }

    private void publishCacheEvent(CacheEvent.EventType eventType, String key, boolean hit, long executionTime,
                                   String userId, String info) {
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

    private void publishRateLimitEvent(String userId, String operation, int currentCount, int maxRequests,
                                       boolean exceeded, long executionTime) {
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
