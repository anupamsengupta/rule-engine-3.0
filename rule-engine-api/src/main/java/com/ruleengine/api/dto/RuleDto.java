package com.ruleengine.api.dto;

import java.util.List;

/**
 * DTO for Rule representation in API layer.
 * References conditions by their IDs.
 *
 * Module: rule-engine-api
 * Layer: API
 */
public record RuleDto(
        String id,
        String name,
        List<String> conditionIds,
        RuleMetadataDto metadata
) {
}

