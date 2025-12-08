package com.ruleengine.application.service;

import com.ruleengine.domain.command.ValidateRuleCommand;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.RuleEvaluationException;
import com.ruleengine.domain.factory.EngineType;
import com.ruleengine.domain.rule.Condition;
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
    private final ConditionService conditionService;

    public RuleEngineService(EngineStrategyRegistry strategyRegistry, EngineType defaultEngineType, ConditionService conditionService) {
        if (strategyRegistry == null) {
            throw new IllegalArgumentException("Strategy registry cannot be null");
        }
        if (defaultEngineType == null) {
            throw new IllegalArgumentException("Default engine type cannot be null");
        }
        if (conditionService == null) {
            throw new IllegalArgumentException("Condition service cannot be null");
        }
        this.strategyRegistry = strategyRegistry;
        this.defaultEngineType = defaultEngineType;
        this.conditionService = conditionService;
    }

    /**
     * Validates a single rule against the given evaluation context.
     */
    public RuleValidationResult validateRule(Rule rule, EvaluationContext context) throws RuleEvaluationException {
        // Fetch conditions by their IDs
        List<Condition> conditions = conditionService.getConditionsByIds(rule.conditionIds());
        return validateRule(rule, context, defaultEngineType, conditions);
    }

    /**
     * Validates a single rule against the given evaluation context with provided conditions.
     * Used for ad-hoc validation with inline conditions.
     */
    public RuleValidationResult validateRule(Rule rule, EvaluationContext context, List<Condition> conditions) throws RuleEvaluationException {
        return validateRule(rule, context, defaultEngineType, conditions);
    }

    /**
     * Validates a single rule against the given evaluation context using a specific engine type with provided conditions.
     */
    public RuleValidationResult validateRule(Rule rule, EvaluationContext context, EngineType engineType, List<Condition> conditions) throws RuleEvaluationException {
        ExpressionEvaluationStrategy strategy = strategyRegistry
                .getExpressionStrategy(engineType)
                .orElseThrow(() -> new RuleEvaluationException(
                    "No expression strategy found for engine type: " + engineType
                ));

        ValidateRuleCommand command = new ValidateRuleCommand(rule, context, strategy, conditions);
        return command.execute();
    }


    /**
     * Validates a rule set against the given evaluation context.
     * Uses the engine type specified in the RuleSet.
     */
    public List<RuleValidationResult> validateRuleSet(
            RuleSet ruleSet,
            EvaluationContext context
    ) throws RuleEvaluationException {
        // Use the engine type from the RuleSet, fallback to default if not specified
        EngineType engineType = ruleSet.engineType() != null ? ruleSet.engineType() : defaultEngineType;
        return validateRuleSet(ruleSet, context, engineType);
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
            // Fetch conditions by their IDs
            List<Condition> conditions = conditionService.getConditionsByIds(rule.conditionIds());
            
            ValidateRuleCommand command = new ValidateRuleCommand(rule, context, strategy, conditions);
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

