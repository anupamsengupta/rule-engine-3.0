package com.ruleengine.domain.rule;

import com.ruleengine.domain.factory.EngineType;

import java.util.List;

/**
 * Represents a collection of rules that can be evaluated together.
 * Supports evaluation policies (e.g., evaluate all rules, stop on first failure).
 * Specifies which engine type to use for evaluation.
 * Can be categorized to allow filtering and applying all rule sets for a category.
 *
 * @param id                  Unique identifier for the rule set
 * @param name                Human-readable name
 * @param rules               List of rules in this set
 * @param stopOnFirstFailure  Whether to stop evaluation on the first failure
 * @param engineType          The engine type to use for evaluating rules in this set
 * @param ruleCategory        Category of the rule set (e.g., "Pricing", "Validation", "Authorization")
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public record RuleSet(
        String id,
        String name,
        List<Rule> rules,
        boolean stopOnFirstFailure,
        EngineType engineType,
        String ruleCategory
) {
    public RuleSet {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("RuleSet id cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("RuleSet name cannot be null or blank");
        }
        if (rules == null || rules.isEmpty()) {
            throw new IllegalArgumentException("RuleSet must contain at least one rule");
        }
        if (engineType == null) {
            throw new IllegalArgumentException("RuleSet engineType cannot be null");
        }
        if (ruleCategory == null || ruleCategory.isBlank()) {
            throw new IllegalArgumentException("RuleSet ruleCategory cannot be null or blank");
        }
    }

    /**
     * Creates a RuleSet with default engine type (SPEL).
     */
    public static RuleSet withDefaultEngine(String id, String name, List<Rule> rules, boolean stopOnFirstFailure, String ruleCategory) {
        return new RuleSet(id, name, rules, stopOnFirstFailure, EngineType.SPEL, ruleCategory);
    }
}

