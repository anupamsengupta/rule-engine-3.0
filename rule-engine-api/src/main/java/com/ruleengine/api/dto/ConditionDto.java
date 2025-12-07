package com.ruleengine.api.dto;

/**
 * DTO for Condition representation in API layer.
 *
 * Module: rule-engine-api
 * Layer: API
 */
public record ConditionDto(
        String attributeCode,
        String attributeType,
        String operator,
        Object targetValue
) {
}

