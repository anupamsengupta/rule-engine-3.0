package com.ruleengine.persistence.mapper;

import com.ruleengine.domain.rule.Rule;
import com.ruleengine.domain.rule.RuleMetadata;
import com.ruleengine.persistence.entity.RuleEntity;

import java.util.List;
import java.util.Set;

/**
 * Mapper for converting between Rule domain model and RuleEntity.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
public final class RuleMapper {

    private RuleMapper() {
        // Utility class
    }

    /**
     * Converts a RuleEntity to a domain Rule.
     */
    public static Rule toDomain(RuleEntity entity) {
        if (entity == null) {
            return null;
        }

        List<String> conditionIds = entity.getConditionIds() != null ? 
                new java.util.ArrayList<>(entity.getConditionIds()) : 
                new java.util.ArrayList<>();

        RuleMetadata metadata = new RuleMetadata(
            entity.getPriority(),
            entity.getActive(),
            entity.getTags() != null ? entity.getTags() : Set.of()
        );

        return new Rule(
            entity.getId(),
            entity.getName(),
            conditionIds,
            metadata
        );
    }

    /**
     * Converts a domain Rule to a RuleEntity.
     */
    public static RuleEntity toEntity(Rule domain) {
        if (domain == null) {
            return null;
        }

        RuleEntity entity = new RuleEntity(
            domain.id(),
            domain.name(),
            domain.metadata().priority(),
            domain.metadata().active(),
            domain.metadata().tags()
        );

        // Set condition IDs
        entity.setConditionIds(domain.conditionIds() != null ? 
                new java.util.ArrayList<>(domain.conditionIds()) : 
                new java.util.ArrayList<>());

        return entity;
    }
}
