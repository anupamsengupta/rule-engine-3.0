package com.ruleengine.persistence.config;

import com.ruleengine.persistence.cache.CacheStrategy;
import com.ruleengine.persistence.cache.CaffeineCacheStrategy;
import com.ruleengine.persistence.cache.RedisCacheStrategy;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA and caching configuration for persistence layer.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.ruleengine.persistence.repository")
@EnableTransactionManagement
public class PersistenceConfiguration {

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    @Primary
    public CacheStrategy caffeineCacheStrategy() {
        return new CaffeineCacheStrategy(10_000, 30);
    }

    @Bean
    public CacheStrategy redisCacheStrategy(
            @Value("${redis.host:localhost}") String redisHost,
            @Value("${redis.port:6379}") int redisPort
    ) {
        return new RedisCacheStrategy(redisHost, redisPort, 1800);
    }
}

