package com.ruleengine.persistence.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * In-memory cache strategy using Caffeine.
 * Provides fast local caching for frequently accessed rules and expressions.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
public class CaffeineCacheStrategy implements CacheStrategy {
    private final Cache<String, Object> cache;

    public CaffeineCacheStrategy() {
        this.cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();
    }

    public CaffeineCacheStrategy(long maximumSize, long expireAfterWriteMinutes) {
        this.cache = Caffeine.newBuilder()
            .maximumSize(maximumSize)
            .expireAfterWrite(expireAfterWriteMinutes, TimeUnit.MINUTES)
            .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = cache.getIfPresent(key);
        if (value != null && type.isInstance(value)) {
            return Optional.of((T) value);
        }
        return Optional.empty();
    }

    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public void evict(String key) {
        cache.invalidate(key);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }
}

