package com.ruleengine.api.dto;

/**
 * Response DTO for expression evaluation.
 * 
 * Module: rule-engine-api
 * Layer: API
 */
public record ExpressionEvaluationResponse(
        Object value,
        String type,
        String error
) {}

