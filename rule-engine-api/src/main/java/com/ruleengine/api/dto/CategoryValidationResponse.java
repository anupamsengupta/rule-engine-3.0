package com.ruleengine.api.dto;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for category-based rule set validation.
 * Contains the overall result (AND of all rule sets) and individual rule set results.
 * 
 * Module: rule-engine-api
 * Layer: API
 */
public record CategoryValidationResponse(
        boolean passed,
        String message,
        String ruleCategory,
        int totalRuleSets,
        int passedRuleSets,
        int failedRuleSets,
        List<RuleSetValidationResult> ruleSetResults,
        Map<String, Object> details
) {
    /**
     * Result for a single rule set validation.
     */
    public record RuleSetValidationResult(
            String ruleSetId,
            String ruleSetName,
            boolean passed,
            String message,
            List<RuleValidationResult> ruleResults
    ) {
    }

    /**
     * Result for a single rule validation within a rule set.
     */
    public record RuleValidationResult(
            String ruleId,
            String ruleName,
            boolean passed,
            String message
    ) {
    }
}

