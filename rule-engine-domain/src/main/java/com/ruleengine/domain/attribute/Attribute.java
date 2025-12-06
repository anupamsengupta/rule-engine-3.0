package com.ruleengine.domain.attribute;

import java.util.Map;
import java.util.Optional;

/**
 * Domain model representing an attribute that can be used in rules and expressions.
 * Attributes are framework-agnostic and immutable.
 *
 * @param code        Stable identifier for the attribute (e.g., "customer.age", "order.total")
 * @param type        Data type of the attribute
 * @param description Optional human-readable description
 * @param constraints Optional constraints (e.g., allowed values, min/max)
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public record Attribute(
        String code,
        String path, 
        AttributeType type,
        Optional<String> description,
        Optional<Map<String, Object>> constraints
) {
    /**
     * Creates an attribute with required fields only.
     */
    public Attribute {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Attribute code cannot be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Attribute type cannot be null");
        }
        if (path == null) {
            path = code;
        }
    }

    /**
     * Creates an attribute with code and type only.
     */
    public Attribute(String code, AttributeType type) {
        this(code, code, type, Optional.empty(), Optional.empty());
    }

    /**
     * Creates an attribute with description.
     */
    public Attribute(String code, AttributeType type, String description) {
        this(code, code, type, Optional.ofNullable(description), Optional.empty());
    }

    /**
     * Creates an attribute with description and constraints.
     */
    public Attribute(String code, AttributeType type, String description, Map<String, Object> constraints) {
        this(code, code, type, Optional.ofNullable(description), Optional.ofNullable(constraints));
    }
    /**
     * Creates an attribute with description and constraints.
     */
    public Attribute(String code, String path, AttributeType type, String description, Map<String, Object> constraints) {
        this(code, path, type, Optional.ofNullable(description), Optional.ofNullable(constraints));
    }
}

