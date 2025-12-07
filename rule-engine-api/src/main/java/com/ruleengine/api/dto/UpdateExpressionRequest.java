package com.ruleengine.api.dto;

/**
 * DTO for updating an existing Expression.
 *
 * Module: rule-engine-api
 * Layer: API
 */
public record UpdateExpressionRequest(
        String expressionString,
        String description
) {
}

