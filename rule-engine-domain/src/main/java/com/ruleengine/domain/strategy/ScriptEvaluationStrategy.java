package com.ruleengine.domain.strategy;

import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.ExpressionEvaluationException;
import com.ruleengine.domain.expression.ExpressionEvaluationResult;
import com.ruleengine.domain.factory.EngineType;

/**
 * Strategy interface for evaluating scripts (e.g., Groovy scripts).
 * Similar to ExpressionEvaluationStrategy but tailored for scripting languages
 * that may support more complex logic and helper methods.
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public interface ScriptEvaluationStrategy {
    /**
     * Evaluates a script string against the given evaluation context.
     *
     * @param script  The script string to evaluate
     * @param context The evaluation context containing attribute values
     * @return ExpressionEvaluationResult containing the result or error
     * @throws com.ruleengine.domain.exception.ExpressionEvaluationException if evaluation fails
     */
    ExpressionEvaluationResult evaluate(String script, EvaluationContext context) throws ExpressionEvaluationException;

    /**
     * Indicates whether this strategy supports the given engine type.
     *
     * @param engineType The engine type to check
     * @return true if this strategy supports the engine type, false otherwise
     */
    boolean supports(EngineType engineType);
}

