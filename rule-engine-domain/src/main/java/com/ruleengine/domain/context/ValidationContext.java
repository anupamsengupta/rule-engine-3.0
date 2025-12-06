package com.ruleengine.domain.context;

import java.util.Map;

/**
 * Context for rule evaluation, containing the input payload and contextual attributes.
 * 
 * The payload is the primary object being validated (e.g., a customer, trade, order).
 * Context attributes provide additional information needed for validation (userId, market, country, etc.).
 * 
 * @param <T> the type of the payload being validated
 */
public record ValidationContext<T>(
        T payload,
        Map<String, Object> contextAttributes
) {
    
    public ValidationContext {
        if (payload == null) {
            throw new IllegalArgumentException("payload cannot be null");
        }
        if (contextAttributes == null) {
            contextAttributes = Map.of();
        }
    }
    
    /**
     * Creates a validation context with only a payload (no additional attributes).
     */
    public ValidationContext(T payload) {
        this(payload, Map.of());
    }
    
    /**
     * Gets the payload cast to the expected type.
     * 
     * @param type the expected type
     * @return the payload cast to the type
     * @throws ClassCastException if the payload is not of the expected type
     */
    @SuppressWarnings("unchecked")
    public <U> U getPayload(Class<U> type) {
        return type.cast(payload);
    }
    
    /**
     * Gets a context attribute by key, cast to the expected type.
     * 
     * @param key the attribute key
     * @param type the expected type
     * @return the attribute value, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <U> U getAttribute(String key, Class<U> type) {
        Object value = contextAttributes.get(key);
        return value != null ? type.cast(value) : null;
    }
    
    /**
     * Checks if a context attribute exists.
     * 
     * @param key the attribute key
     * @return true if the attribute exists
     */
    public boolean hasAttribute(String key) {
        return contextAttributes.containsKey(key);
    }
}

