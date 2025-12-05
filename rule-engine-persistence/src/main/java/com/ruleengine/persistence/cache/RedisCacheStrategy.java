package com.ruleengine.persistence.cache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Optional;

/**
 * Distributed cache strategy using Redis.
 * Provides shared caching across multiple application instances.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
public class RedisCacheStrategy implements CacheStrategy {
    private final JedisPool jedisPool;
    private final int defaultTtlSeconds;

    public RedisCacheStrategy(String host, int port) {
        this(host, port, 1800); // Default 30 minutes
    }

    public RedisCacheStrategy(String host, int port, int defaultTtlSeconds) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(32);
        this.jedisPool = new JedisPool(poolConfig, host, port);
        this.defaultTtlSeconds = defaultTtlSeconds;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        try (Jedis jedis = jedisPool.getResource()) {
            String value = jedis.get(key);
            if (value != null) {
                // Simple deserialization - in production, use proper serialization (JSON, etc.)
                if (type == String.class) {
                    return Optional.of(type.cast(value));
                }
                // Add more type conversions as needed
            }
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, Object value) {
        try (Jedis jedis = jedisPool.getResource()) {
            String stringValue = value != null ? value.toString() : "";
            jedis.setex(key, defaultTtlSeconds, stringValue);
        }
    }

    @Override
    public void evict(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        }
    }

    @Override
    public void clear() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.flushDB();
        }
    }

    public void close() {
        jedisPool.close();
    }
}

