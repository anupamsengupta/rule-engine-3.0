package com.ruleengine.api.dto;

/**
 * DTO for Expression representation in API layer.
 *
 * Module: rule-engine-api
 * Layer: API
 */
public record ExpressionDto(
        String id,
        String expressionString,
        String description
) {
}

