package com.springboot.starter.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.starter.model.cache.CacheEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InfraHealthChecker implements InitializingBean {

    private static final String KAFKA_TEST_TOPIC = "test-topic";
    private String testMessage;

    private final MongoTemplate mongoTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, CacheEvent> kafkaTemplate;
    private final ObjectMapper mapper;

    @KafkaListener(topics = KAFKA_TEST_TOPIC, groupId = "infrastructure-checker")
    public void receiveTestKafkaMessage(String message) throws JsonProcessingException {
        var msg = mapper.readValue(message, CacheEvent.class);
        if (msg.eventType().equals(testMessage)) {
            log.info("[infra-check] ✅ Kafka connection successful. Test message sent and received from topic: {}, " +
                    "message: {}",
                KAFKA_TEST_TOPIC, msg);
        } else {
            log.warn("[infra-check] Kafka message received but content mismatch. Sent: {}, Received: {}",
                message, testMessage);
        }
    }

    @Override
    public void afterPropertiesSet() {
        checkMongoConnection();
        checkRedisConnection();
        sendKafkaMessage();
    }

    private void checkMongoConnection() {
        var result = mongoTemplate.getCollectionNames();
        log.info("[infra-check] ✅ MongoDB connection successful. Collections: {}", result);
    }

    private void checkRedisConnection() {
        String testKey = "test:connection";
        String testValue = "Connection test at " + System.currentTimeMillis();

        redisTemplate.opsForValue().set(testKey, testValue);
        Object retrievedValue = redisTemplate.opsForValue().get(testKey);

        if (testValue.equals(retrievedValue)) {
            log.info("[infra-check] ✅ Redis connection successful. Test value correctly stored and retrieved.");
        }

        redisTemplate.delete(testKey);
    }

    private void sendKafkaMessage() {
        testMessage = "Test message at " + System.currentTimeMillis();
        log.info("[infra-check] Sending Kafka test message: {}", testMessage);
        kafkaTemplate.send(KAFKA_TEST_TOPIC, CacheEvent.builder().eventType(testMessage).build());
    }
}