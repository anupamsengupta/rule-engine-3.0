package com.ruleengine.domain.rule;

import com.ruleengine.domain.attribute.Attribute;
import com.ruleengine.domain.operator.ComparisonOperator;

/**
 * Represents a single condition in a rule.
 * A condition compares an attribute value against a target value using a comparison operator.
 *
 * @param attribute         The attribute to evaluate
 * @param operator          The comparison operator to apply
 * @param targetValue       The value to compare against
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public record Condition(
        Attribute attribute,
        ComparisonOperator operator,
        Object targetValue
) {
    public Condition {
        if (attribute == null) {
            throw new IllegalArgumentException("Condition attribute cannot be null");
        }
        if (operator == null) {
            throw new IllegalArgumentException("Condition operator cannot be null");
        }
    }
}

