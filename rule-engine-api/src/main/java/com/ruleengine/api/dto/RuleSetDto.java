package com.ruleengine.api.dto;

import java.util.List;

/**
 * DTO for RuleSet representation in API layer.
 *
 * Module: rule-engine-api
 * Layer: API
 */
public record RuleSetDto(
        String id,
        String name,
        List<String> ruleIds,
        Boolean stopOnFirstFailure,
        String engineType
) {
}

