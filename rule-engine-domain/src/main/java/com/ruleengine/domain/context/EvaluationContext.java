package com.ruleengine.domain.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Evaluation context providing attribute values for rule validation and expression evaluation.
 * Immutable wrapper around a map of attribute codes to their values.
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public final class EvaluationContext {
    private final Map<String, Object> attributeValues;

    private EvaluationContext(Map<String, Object> attributeValues) {
        //this.attributeValues = Collections.unmodifiableMap(new HashMap<>(attributeValues));
        this.attributeValues = new HashMap<>(attributeValues);
    }

    /**
     * Creates an evaluation context from a map of attribute values.
     *
     * @param attributeValues Map where key is attribute code and value is the actual value
     * @return New EvaluationContext instance
     */
    public static EvaluationContext from(Map<String, Object> attributeValues) {
        return new EvaluationContext(attributeValues != null ? attributeValues : Map.of());
    }

    /**
     * Creates an empty evaluation context.
     */
    public static EvaluationContext empty() {
        return new EvaluationContext(Map.of());
    }

    /**
     * Gets the value for a given attribute code.
     *
     * @param attributeCode The attribute code
     * @return The value, or null if not present
     */
    public Object getValue(String attributeCode) {
        return attributeValues.get(attributeCode);
    }

    /**
     * Checks if a value exists for the given attribute code.
     */
    public boolean hasValue(String attributeCode) {
        return attributeValues.containsKey(attributeCode);
    }

    /**
     * Returns an unmodifiable view of all attribute values.
     */
    public Map<String, Object> getAllValues() {
        return attributeValues;
    }
}

