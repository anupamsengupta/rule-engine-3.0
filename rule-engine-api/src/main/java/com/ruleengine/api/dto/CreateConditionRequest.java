package com.ruleengine.api.dto;

/**
 * DTO for creating a new Condition.
 * Either rightAttributeCode OR targetValue must be provided, but not both.
 *
 * Module: rule-engine-api
 * Layer: API
 */
public record CreateConditionRequest(
        String id,
        String name,
        String leftAttributeCode,
        String operator,
        String rightAttributeCode,
        Object targetValue
) {
}

