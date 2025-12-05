package com.ruleengine.infrastructure.factory;

import com.ruleengine.domain.factory.EngineType;
import com.ruleengine.domain.strategy.ExpressionEvaluationStrategy;
import com.ruleengine.domain.strategy.ScriptEvaluationStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for mapping EngineType to strategy implementations.
 * Implements the enum-based factory pattern for selecting evaluation strategies.
 * This registry is mutable to allow dynamic registration of strategies.
 * 
 * Module: rule-engine-infrastructure
 * Layer: Infrastructure
 */
public class EngineStrategyRegistry {
    private final Map<EngineType, ExpressionEvaluationStrategy> expressionStrategies = new HashMap<>();
    private final Map<EngineType, ScriptEvaluationStrategy> scriptStrategies = new HashMap<>();

    /**
     * Registers an expression evaluation strategy for the given engine type.
     *
     * @param engineType The engine type
     * @param strategy   The strategy implementation
     */
    public void registerExpressionStrategy(EngineType engineType, ExpressionEvaluationStrategy strategy) {
        if (engineType == null) {
            throw new IllegalArgumentException("Engine type cannot be null");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        if (!strategy.supports(engineType)) {
            throw new IllegalArgumentException(
                "Strategy does not support engine type: " + engineType
            );
        }
        expressionStrategies.put(engineType, strategy);
    }

    /**
     * Registers a script evaluation strategy for the given engine type.
     *
     * @param engineType The engine type
     * @param strategy   The strategy implementation
     */
    public void registerScriptStrategy(EngineType engineType, ScriptEvaluationStrategy strategy) {
        if (engineType == null) {
            throw new IllegalArgumentException("Engine type cannot be null");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        if (!strategy.supports(engineType)) {
            throw new IllegalArgumentException(
                "Strategy does not support engine type: " + engineType
            );
        }
        scriptStrategies.put(engineType, strategy);
    }

    /**
     * Gets an expression evaluation strategy for the given engine type.
     *
     * @param engineType The engine type
     * @return Optional containing the strategy if found
     */
    public Optional<ExpressionEvaluationStrategy> getExpressionStrategy(EngineType engineType) {
        return Optional.ofNullable(expressionStrategies.get(engineType));
    }

    /**
     * Gets a script evaluation strategy for the given engine type.
     *
     * @param engineType The engine type
     * @return Optional containing the strategy if found
     */
    public Optional<ScriptEvaluationStrategy> getScriptStrategy(EngineType engineType) {
        return Optional.ofNullable(scriptStrategies.get(engineType));
    }

    /**
     * Checks if an expression strategy is registered for the given engine type.
     */
    public boolean hasExpressionStrategy(EngineType engineType) {
        return expressionStrategies.containsKey(engineType);
    }

    /**
     * Checks if a script strategy is registered for the given engine type.
     */
    public boolean hasScriptStrategy(EngineType engineType) {
        return scriptStrategies.containsKey(engineType);
    }
}

