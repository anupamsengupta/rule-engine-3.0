package com.ruleengine.api.dto;

import java.util.Map;

/**
 * Response DTO for rule validation.
 * 
 * Module: rule-engine-api
 * Layer: API
 */
public record RuleValidationResponse(
        boolean passed,
        String message,
        Map<String, Object> details
) {}

