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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Validates all rule sets for a given category against the evaluation context.
     * Returns an aggregated result where overall success requires ALL rule sets to pass (AND operation).
     * 
     * @param ruleSets List of rule sets for the category
     * @param context The evaluation context containing attribute values
     * @return Aggregated validation result with details of each rule set
     * @throws RuleEvaluationException if validation fails
     */
    public CategoryValidationResult validateRuleSetsByCategory(
            List<RuleSet> ruleSets,
            EvaluationContext context
    ) throws RuleEvaluationException {
        if (ruleSets == null || ruleSets.isEmpty()) {
            return new CategoryValidationResult(
                    true, // Empty category is considered valid
                    "No rule sets found for category",
                    new ArrayList<>(),
                    new HashMap<>()
            );
        }

        List<RuleSetValidationResult> ruleSetResults = new ArrayList<>();
        boolean overallPassed = true;
        int totalRuleSets = ruleSets.size();
        int passedRuleSets = 0;
        int failedRuleSets = 0;

        for (RuleSet ruleSet : ruleSets) {
            try {
                List<RuleValidationResult> ruleResults = validateRuleSet(ruleSet, context);
                
                // Determine if this rule set passed (all rules must pass)
                boolean ruleSetPassed = ruleResults.stream().allMatch(RuleValidationResult::passed);
                
                if (ruleSetPassed) {
                    passedRuleSets++;
                } else {
                    failedRuleSets++;
                    overallPassed = false; // AND operation: any failure means overall failure
                }

                // Build rule results for this rule set
                List<RuleResult> ruleResultList = new ArrayList<>();
                for (int i = 0; i < ruleSet.rules().size() && i < ruleResults.size(); i++) {
                    Rule rule = ruleSet.rules().get(i);
                    RuleValidationResult result = ruleResults.get(i);
                    ruleResultList.add(new RuleResult(
                            rule.id(),
                            rule.name(),
                            result.passed(),
                            result.message().orElse(result.passed() ? "Rule passed" : "Rule failed")
                    ));
                }

                ruleSetResults.add(new RuleSetValidationResult(
                        ruleSet.id(),
                        ruleSet.name(),
                        ruleSetPassed,
                        ruleSetPassed ? "All rules in rule set passed" : "One or more rules in rule set failed",
                        ruleResultList
                ));
            } catch (RuleEvaluationException e) {
                failedRuleSets++;
                overallPassed = false;
                ruleSetResults.add(new RuleSetValidationResult(
                        ruleSet.id(),
                        ruleSet.name(),
                        false,
                        "Rule set validation error: " + e.getMessage(),
                        new ArrayList<>()
                ));
            }
        }

        String message = overallPassed 
                ? String.format("All %d rule set(s) passed validation", totalRuleSets)
                : String.format("%d of %d rule set(s) failed validation", failedRuleSets, totalRuleSets);

        Map<String, Object> details = new HashMap<>();
        details.put("totalRuleSets", totalRuleSets);
        details.put("passedRuleSets", passedRuleSets);
        details.put("failedRuleSets", failedRuleSets);

        return new CategoryValidationResult(
                overallPassed,
                message,
                ruleSetResults,
                details
        );
    }

    /**
     * Result of validating all rule sets for a category.
     */
    public record CategoryValidationResult(
            boolean passed,
            String message,
            List<RuleSetValidationResult> ruleSetResults,
            Map<String, Object> details
    ) {
    }

    /**
     * Result for a single rule set validation.
     */
    public record RuleSetValidationResult(
            String ruleSetId,
            String ruleSetName,
            boolean passed,
            String message,
            List<RuleResult> ruleResults
    ) {
    }

    /**
     * Result for a single rule validation within a rule set.
     */
    public record RuleResult(
            String ruleId,
            String ruleName,
            boolean passed,
            String message
    ) {
    }
}

