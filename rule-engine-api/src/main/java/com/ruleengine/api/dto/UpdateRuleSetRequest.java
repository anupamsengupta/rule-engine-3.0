package com.ruleengine.api.dto;

import java.util.List;

/**
 * DTO for updating an existing RuleSet.
 *
 * Module: rule-engine-api
 * Layer: API
 */
public record UpdateRuleSetRequest(
        String name,
        List<String> ruleIds,
        Boolean stopOnFirstFailure,
        String engineType
) {
}

