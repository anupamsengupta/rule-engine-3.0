package com.ruleengine.api.dto;

import java.util.List;
import java.util.Set;

/**
 * DTO for updating an existing Rule.
 * References conditions by their IDs.
 *
 * Module: rule-engine-api
 * Layer: API
 */
public record UpdateRuleRequest(
        String name,
        List<String> conditionIds,
        Integer priority,
        Boolean active,
        Set<String> tags
) {
}

