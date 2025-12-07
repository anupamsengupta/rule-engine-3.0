package com.ruleengine.api.dto;

import java.util.Map;

/**
 * DTO for Attribute representation in API layer.
 *
 * Module: rule-engine-api
 * Layer: API
 */
public record AttributeDto(
        String code,
        String path,
        String type,
        String description,
        Map<String, Object> constraints
) {
}

