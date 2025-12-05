package com.ruleengine.domain.engine;

import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.expression.ExpressionEvaluationResult;

/**
 * Interface for expression evaluation engine.
 * Provides operations to evaluate expressions against evaluation contexts.
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public interface ExpressionEngine {
    /**
     * Evaluates an expression by its identifier.
     *
     * @param expressionId The identifier of the expression to evaluate
     * @param context      The evaluation context containing attribute values
     * @return ExpressionEvaluationResult containing the evaluated value or error
     * @throws com.ruleengine.domain.exception.ExpressionEvaluationException if evaluation fails
     */
    ExpressionEvaluationResult evaluateExpression(String expressionId, EvaluationContext context);

    /**
     * Evaluates an expression string directly.
     *
     * @param expression The expression string to evaluate
     * @param context    The evaluation context containing attribute values
     * @return ExpressionEvaluationResult containing the evaluated value or error
     * @throws com.ruleengine.domain.exception.ExpressionEvaluationException if evaluation fails
     */
    ExpressionEvaluationResult evaluateExpressionString(String expression, EvaluationContext context);
}

