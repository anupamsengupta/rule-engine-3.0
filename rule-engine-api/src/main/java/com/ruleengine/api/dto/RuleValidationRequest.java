package com.ruleengine.api.dto;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for rule validation.
 * 
 * Module: rule-engine-api
 * Layer: API
 */
public record RuleValidationRequest(
        String ruleId,
        String ruleName,
        List<ConditionDto> conditions,
        Map<String, Object> context
) {
    public record ConditionDto(
            String attributeCode,
            String attributeType,
            String operator,
            Object targetValue
    ) {}
}

