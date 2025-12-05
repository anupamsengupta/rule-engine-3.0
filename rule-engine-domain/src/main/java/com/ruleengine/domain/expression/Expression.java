package com.ruleengine.domain.expression;

import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.ExpressionEvaluationException;
import com.ruleengine.domain.strategy.ExpressionEvaluationStrategy;

import java.util.Optional;

/**
 * Domain model representing an expression that can be evaluated against an evaluation context.
 * Expressions are immutable and delegate actual evaluation logic to strategies.
 *
 * @param id              Optional unique identifier for the expression
 * @param expressionString The expression string to evaluate
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public record Expression(
        Optional<String> id,
        String expressionString
) {
    public Expression {
        if (expressionString == null || expressionString.isBlank()) {
            throw new IllegalArgumentException("Expression string cannot be null or blank");
        }
        if (id != null && id.isPresent() && id.get().isBlank()) {
            throw new IllegalArgumentException("Expression id cannot be blank if provided");
        }
    }

    /**
     * Creates an expression without an ID.
     */
    public Expression(String expressionString) {
        this(Optional.empty(), expressionString);
    }

    /**
     * Evaluates this expression against the given evaluation context using the provided strategy.
     *
     * @param context  The evaluation context containing attribute values
     * @param strategy The strategy to use for evaluation
     * @return ExpressionEvaluationResult containing the evaluated value or error
     */
    public ExpressionEvaluationResult evaluate(EvaluationContext context, ExpressionEvaluationStrategy strategy) throws ExpressionEvaluationException {
        return strategy.evaluate(expressionString, context);
    }
}

