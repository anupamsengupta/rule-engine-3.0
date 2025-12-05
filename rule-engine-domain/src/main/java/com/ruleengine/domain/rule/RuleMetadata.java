package com.ruleengine.domain.rule;

import java.util.Set;

/**
 * Metadata associated with a rule.
 *
 * @param priority  Priority level (higher = more important)
 * @param active    Whether the rule is currently active
 * @param tags      Optional tags for categorization
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public record RuleMetadata(
        int priority,
        boolean active,
        Set<String> tags
) {
    public RuleMetadata {
        if (tags == null) {
            tags = Set.of();
        }
    }

    /**
     * Creates metadata with default values (priority=0, active=true, no tags).
     */
    public static RuleMetadata defaults() {
        return new RuleMetadata(0, true, Set.of());
    }
}

