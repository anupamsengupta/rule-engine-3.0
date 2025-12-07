package com.ruleengine.api.dto;

import java.util.Set;

/**
 * DTO for RuleMetadata representation in API layer.
 *
 * Module: rule-engine-api
 * Layer: API
 */
public record RuleMetadataDto(
        Integer priority,
        Boolean active,
        Set<String> tags
) {
}

