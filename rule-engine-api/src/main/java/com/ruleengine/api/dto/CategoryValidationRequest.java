package com.ruleengine.api.dto;

import java.util.Map;

/**
 * Request DTO for category-based rule set validation.
 * Validates all rule sets for a given category against the provided context.
 * 
 * Module: rule-engine-api
 * Layer: API
 */
public record CategoryValidationRequest(
        String ruleCategory,
        Map<String, Object> contextMap
) {
}

