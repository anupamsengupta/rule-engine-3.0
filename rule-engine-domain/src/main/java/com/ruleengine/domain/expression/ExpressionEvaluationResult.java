package com.ruleengine.domain.expression;

import com.ruleengine.domain.attribute.AttributeType;

import java.util.Optional;

/**
 * Result of an expression evaluation operation.
 *
 * @param value The evaluated value (may be of any type)
 * @param type  The type of the result
 * @param error Optional error message if evaluation failed
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public record ExpressionEvaluationResult(
        Object value,
        AttributeType type,
        Optional<String> error
) {
    /**
     * Creates a successful evaluation result.
     */
    public static ExpressionEvaluationResult success(Object value, AttributeType type) {
        return new ExpressionEvaluationResult(value, type, Optional.empty());
    }

    /**
     * Creates a failed evaluation result with an error message.
     */
    public static ExpressionEvaluationResult failure(String error) {
        return new ExpressionEvaluationResult(null, AttributeType.STRING, Optional.of(error));
    }
}

