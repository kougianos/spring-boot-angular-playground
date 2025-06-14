package com.springboot.starter.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    private static final String TEST_TOPIC = "test-topic";

    @Bean
    public NewTopic infrastructureTestTopic() {
        return TopicBuilder
                .name(TEST_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
