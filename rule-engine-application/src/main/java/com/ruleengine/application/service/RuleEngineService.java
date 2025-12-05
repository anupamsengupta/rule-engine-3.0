package com.ruleengine.application.service;

import com.ruleengine.domain.command.ValidateRuleCommand;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.RuleEvaluationException;
import com.ruleengine.domain.factory.EngineType;
import com.ruleengine.domain.rule.Rule;
import com.ruleengine.domain.rule.RuleSet;
import com.ruleengine.domain.rule.RuleValidationResult;
import com.ruleengine.domain.strategy.ExpressionEvaluationStrategy;
import com.ruleengine.infrastructure.factory.EngineStrategyRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * Application service for orchestrating rule validation operations.
 * Coordinates domain strategies and commands to implement use cases.
 * 
 * Module: rule-engine-application
 * Layer: Application
 */
public class RuleEngineService {
    private final EngineStrategyRegistry strategyRegistry;
    private final EngineType defaultEngineType;

    public RuleEngineService(EngineStrategyRegistry strategyRegistry, EngineType defaultEngineType) {
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
     * Validates a single rule against the given evaluation context.
     */
    public RuleValidationResult validateRule(Rule rule, EvaluationContext context) throws RuleEvaluationException {
        return validateRule(rule, context, defaultEngineType);
    }

    /**
     * Validates a single rule against the given evaluation context using a specific engine type.
     */
    public RuleValidationResult validateRule(
            Rule rule,
            EvaluationContext context,
            EngineType engineType
    ) throws RuleEvaluationException {
        ExpressionEvaluationStrategy strategy = strategyRegistry
                .getExpressionStrategy(engineType)
                .orElseThrow(() -> new RuleEvaluationException(
                    "No expression strategy found for engine type: " + engineType
                ));

        ValidateRuleCommand command = new ValidateRuleCommand(rule, context, strategy);
        return command.execute();
    }

    /**
     * Validates a rule set against the given evaluation context.
     */
    public List<RuleValidationResult> validateRuleSet(
            RuleSet ruleSet,
            EvaluationContext context
    ) throws RuleEvaluationException {
        return validateRuleSet(ruleSet, context, defaultEngineType);
    }

    /**
     * Validates a rule set against the given evaluation context using a specific engine type.
     */
    public List<RuleValidationResult> validateRuleSet(
            RuleSet ruleSet,
            EvaluationContext context,
            EngineType engineType
    ) throws RuleEvaluationException {
        ExpressionEvaluationStrategy strategy = strategyRegistry
                .getExpressionStrategy(engineType)
                .orElseThrow(() -> new RuleEvaluationException(
                    "No expression strategy found for engine type: " + engineType
                ));

        List<RuleValidationResult> results = new ArrayList<>();

        for (Rule rule : ruleSet.rules()) {
            ValidateRuleCommand command = new ValidateRuleCommand(rule, context, strategy);
            RuleValidationResult result = command.execute();

            results.add(result);

            // Stop on first failure if configured
            if (ruleSet.stopOnFirstFailure() && !result.passed()) {
                break;
            }
        }

        return results;
    }
}

