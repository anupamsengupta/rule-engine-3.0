package com.ruleengine.domain.rule;

import com.ruleengine.domain.attribute.Attribute;
import com.ruleengine.domain.operator.ComparisonOperator;

import java.util.Optional;

/**
 * Represents a standalone condition that can be reused across multiple rules.
 * A condition can compare:
 * - An attribute against a target value (attribute vs value)
 * - Two attributes against each other (attribute vs attribute)
 *
 * @param id                Unique identifier for the condition
 * @param name              Human-readable name for the condition
 * @param leftAttribute     The left attribute to evaluate (required)
 * @param operator          The comparison operator to apply (required)
 * @param rightAttribute    Optional second attribute (for attribute vs attribute comparison)
 * @param targetValue       Optional target value (for attribute vs value comparison)
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public record Condition(
        String id,
        String name,
        Attribute leftAttribute,
        ComparisonOperator operator,
        Optional<Attribute> rightAttribute,
        Optional<Object> targetValue
) {
    public Condition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Condition id cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Condition name cannot be null or blank");
        }
        if (leftAttribute == null) {
            throw new IllegalArgumentException("Condition leftAttribute cannot be null");
        }
        if (operator == null) {
            throw new IllegalArgumentException("Condition operator cannot be null");
        }
        // Either rightAttribute or targetValue must be present, but not both
        if (rightAttribute.isEmpty() && targetValue.isEmpty()) {
            throw new IllegalArgumentException("Condition must have either rightAttribute or targetValue");
        }
        if (rightAttribute.isPresent() && targetValue.isPresent()) {
            throw new IllegalArgumentException("Condition cannot have both rightAttribute and targetValue");
        }
    }

    /**
     * Creates a condition comparing an attribute against a target value.
     */
    public static Condition attributeVsValue(String id, String name, Attribute attribute, 
                                            ComparisonOperator operator, Object targetValue) {
        return new Condition(id, name, attribute, operator, Optional.empty(), Optional.of(targetValue));
    }

    /**
     * Creates a condition comparing two attributes.
     */
    public static Condition attributeVsAttribute(String id, String name, Attribute leftAttribute,
                                                ComparisonOperator operator, Attribute rightAttribute) {
        return new Condition(id, name, leftAttribute, operator, Optional.of(rightAttribute), Optional.empty());
    }
}
