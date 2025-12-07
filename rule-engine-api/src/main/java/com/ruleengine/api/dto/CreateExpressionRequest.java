package com.ruleengine.api.dto;

/**
 * DTO for creating a new Expression.
 *
 * Module: rule-engine-api
 * Layer: API
 */
public record CreateExpressionRequest(
        String id,
        String expressionString,
        String description
) {
}

