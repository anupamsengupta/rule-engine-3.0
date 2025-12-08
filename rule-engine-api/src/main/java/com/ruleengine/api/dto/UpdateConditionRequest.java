package com.ruleengine.api.dto;

/**
 * DTO for updating an existing Condition.
 * Either rightAttributeCode OR targetValue must be provided, but not both.
 *
 * Module: rule-engine-api
 * Layer: API
 */
public record UpdateConditionRequest(
        String name,
        String leftAttributeCode,
        String operator,
        String rightAttributeCode,
        Object targetValue
) {
}

