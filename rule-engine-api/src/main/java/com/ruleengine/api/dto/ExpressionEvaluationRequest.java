package com.ruleengine.api.dto;

import java.util.Map;

/**
 * Request DTO for expression evaluation.
 * 
 * Module: rule-engine-api
 * Layer: API
 */
public record ExpressionEvaluationRequest(
        String expressionId,
        String expressionString,
        Map<String, Object> context
) {}

