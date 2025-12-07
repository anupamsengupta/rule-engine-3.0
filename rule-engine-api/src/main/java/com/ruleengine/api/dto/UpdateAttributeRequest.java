package com.ruleengine.api.dto;

import java.util.Map;

/**
 * DTO for updating an existing Attribute.
 *
 * Module: rule-engine-api
 * Layer: API
 */
public record UpdateAttributeRequest(
        String path,
        String type,
        String description,
        Map<String, Object> constraints
) {
}

