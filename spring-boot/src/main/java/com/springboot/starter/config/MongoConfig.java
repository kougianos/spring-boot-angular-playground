package com.springboot.starter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.*;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
public class MongoConfig {

    @Bean
    public MappingMongoConverter mappingMongoConverter(
        MongoDatabaseFactory mongoDatabaseFactory,
        MongoMappingContext mongoMappingContext,
        MongoCustomConversions customConversions) {

        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDatabaseFactory);

        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);
        converter.setCustomConversions(customConversions);
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }
}