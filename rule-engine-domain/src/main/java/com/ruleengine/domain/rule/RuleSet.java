package com.ruleengine.domain.rule;

import java.util.List;

/**
 * Represents a collection of rules that can be evaluated together.
 * Supports evaluation policies (e.g., evaluate all rules, stop on first failure).
 *
 * @param id                  Unique identifier for the rule set
 * @param name                Human-readable name
 * @param rules               List of rules in this set
 * @param stopOnFirstFailure  Whether to stop evaluation on the first failure
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public record RuleSet(
        String id,
        String name,
        List<Rule> rules,
        boolean stopOnFirstFailure
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
    }
}

