package com.ruleengine.api.dto;

import java.util.List;
import java.util.Set;

/**
 * DTO for creating a new Rule.
 *
 * Module: rule-engine-api
 * Layer: API
 */
public record CreateRuleRequest(
        String id,
        String name,
        List<ConditionDto> conditions,
        Integer priority,
        Boolean active,
        Set<String> tags
) {
}

