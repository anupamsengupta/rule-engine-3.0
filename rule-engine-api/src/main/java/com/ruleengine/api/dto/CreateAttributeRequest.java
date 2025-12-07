package com.ruleengine.api.dto;

import java.util.Map;

/**
 * DTO for creating a new Attribute.
 *
 * Module: rule-engine-api
 * Layer: API
 */
public record CreateAttributeRequest(
        String code,
        String path,
        String type,
        String description,
        Map<String, Object> constraints
) {
}

