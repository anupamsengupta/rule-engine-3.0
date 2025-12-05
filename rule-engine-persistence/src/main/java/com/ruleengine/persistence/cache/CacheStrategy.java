package com.ruleengine.persistence.cache;

import java.util.Optional;

/**
 * Interface for cache strategies.
 * Supports both in-memory (Caffeine) and distributed (Redis) caching.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
public interface CacheStrategy {
    /**
     * Gets a value from the cache.
     *
     * @param key The cache key
     * @return Optional containing the value if found
     */
    <T> Optional<T> get(String key, Class<T> type);

    /**
     * Puts a value into the cache.
     *
     * @param key   The cache key
     * @param value The value to cache
     */
    void put(String key, Object value);

    /**
     * Removes a value from the cache.
     *
     * @param key The cache key
     */
    void evict(String key);

    /**
     * Clears all entries from the cache.
     */
    void clear();
}

