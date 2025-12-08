package com.ruleengine.api.dto;

import java.util.List;

/**
 * DTO for creating a new RuleSet.
 *
 * Module: rule-engine-api
 * Layer: API
 */
public record CreateRuleSetRequest(
        String id,
        String name,
        List<String> ruleIds,
        Boolean stopOnFirstFailure,
        String engineType,
        String ruleCategory
) {
}

