package com.ruleengine.domain.strategy;

import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.ExpressionEvaluationException;
import com.ruleengine.domain.expression.ExpressionEvaluationResult;
import com.ruleengine.domain.factory.EngineType;

/**
 * Strategy interface for evaluating expressions using various expression languages.
 * Implementations should handle expression compilation and evaluation, bridging
 * attribute definitions/values into expression contexts, and wrapping exceptions.
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public interface ExpressionEvaluationStrategy {
    /**
     * Evaluates an expression string against the given evaluation context.
     *
     * @param expression The expression string to evaluate
     * @param context    The evaluation context containing attribute values
     * @return ExpressionEvaluationResult containing the result or error
     * @throws com.ruleengine.domain.exception.ExpressionEvaluationException if evaluation fails
     */
    ExpressionEvaluationResult evaluate(String expression, EvaluationContext context) throws ExpressionEvaluationException;

    /**
     * Indicates whether this strategy supports the given engine type.
     *
     * @param engineType The engine type to check
     * @return true if this strategy supports the engine type, false otherwise
     */
    boolean supports(EngineType engineType);
}

