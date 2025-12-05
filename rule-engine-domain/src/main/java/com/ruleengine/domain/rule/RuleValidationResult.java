package com.ruleengine.domain.rule;

import java.util.Map;
import java.util.Optional;

/**
 * Result of a rule validation operation.
 *
 * @param passed  Whether the rule validation passed
 * @param message Optional human-readable message explaining the result
 * @param details Optional map of additional details (e.g., which conditions failed)
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public record RuleValidationResult(
        boolean passed,
        Optional<String> message,
        Optional<Map<String, Object>> details
) {
    /**
     * Creates a successful validation result.
     */
    public static RuleValidationResult success() {
        return new RuleValidationResult(true, Optional.empty(), Optional.empty());
    }

    /**
     * Creates a successful validation result with a message.
     */
    public static RuleValidationResult success(String message) {
        return new RuleValidationResult(true, Optional.of(message), Optional.empty());
    }

    /**
     * Creates a failed validation result.
     */
    public static RuleValidationResult failure() {
        return new RuleValidationResult(false, Optional.empty(), Optional.empty());
    }

    /**
     * Creates a failed validation result with a message.
     */
    public static RuleValidationResult failure(String message) {
        return new RuleValidationResult(false, Optional.of(message), Optional.empty());
    }

    /**
     * Creates a failed validation result with message and details.
     */
    public static RuleValidationResult failure(String message, Map<String, Object> details) {
        return new RuleValidationResult(false, Optional.of(message), Optional.of(details));
    }
}

