package com.ruleengine.application.service;

import com.ruleengine.domain.command.EvaluateExpressionCommand;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.ExpressionEvaluationException;
import com.ruleengine.domain.expression.Expression;
import com.ruleengine.domain.expression.ExpressionEvaluationResult;
import com.ruleengine.domain.factory.EngineType;
import com.ruleengine.domain.strategy.ExpressionEvaluationStrategy;
import com.ruleengine.domain.strategy.ScriptEvaluationStrategy;
import com.ruleengine.infrastructure.factory.EngineStrategyRegistry;

/**
 * Application service for orchestrating expression evaluation operations.
 * Coordinates domain strategies and commands to implement use cases.
 * 
 * Module: rule-engine-application
 * Layer: Application
 */
public class ExpressionEngineService {
    private final EngineStrategyRegistry strategyRegistry;
    private final EngineType defaultEngineType;

    public ExpressionEngineService(EngineStrategyRegistry strategyRegistry, EngineType defaultEngineType) {
        if (strategyRegistry == null) {
            throw new IllegalArgumentException("Strategy registry cannot be null");
        }
        if (defaultEngineType == null) {
            throw new IllegalArgumentException("Default engine type cannot be null");
        }
        this.strategyRegistry = strategyRegistry;
        this.defaultEngineType = defaultEngineType;
    }

    /**
     * Evaluates an expression against the given evaluation context.
     */
    public ExpressionEvaluationResult evaluateExpression(
            Expression expression,
            EvaluationContext context
    ) throws ExpressionEvaluationException {
        return evaluateExpression(expression, context, defaultEngineType);
    }

    /**
     * Evaluates an expression against the given evaluation context using a specific engine type.
     */
    public ExpressionEvaluationResult evaluateExpression(
            Expression expression,
            EvaluationContext context,
            EngineType engineType
    ) throws ExpressionEvaluationException {
        ExpressionEvaluationStrategy strategy = strategyRegistry
                .getExpressionStrategy(engineType)
                .orElseThrow(() -> new ExpressionEvaluationException(
                    "No expression strategy found for engine type: " + engineType
                ));

        EvaluateExpressionCommand command = new EvaluateExpressionCommand(expression, context, strategy);
        return command.execute();
    }

    /**
     * Evaluates an expression string against the given evaluation context.
     */
    public ExpressionEvaluationResult evaluateExpressionString(
            String expressionString,
            EvaluationContext context
    ) throws ExpressionEvaluationException {
        return evaluateExpressionString(expressionString, context, defaultEngineType);
    }

    /**
     * Evaluates an expression string against the given evaluation context using a specific engine type.
     */
    public ExpressionEvaluationResult evaluateExpressionString(
            String expressionString,
            EvaluationContext context,
            EngineType engineType
    ) throws ExpressionEvaluationException {
        Expression expression = new Expression(expressionString);
        return evaluateExpression(expression, context, engineType);
    }

    /**
     * Evaluates a Groovy script against the given evaluation context.
     */
    public ExpressionEvaluationResult evaluateScript(
            String script,
            EvaluationContext context
    ) throws ExpressionEvaluationException {
        ScriptEvaluationStrategy strategy = strategyRegistry
                .getScriptStrategy(EngineType.GROOVY)
                .orElseThrow(() -> new ExpressionEvaluationException(
                    "No script strategy found for engine type: GROOVY"
                ));

        return strategy.evaluate(script, context);
    }
}

