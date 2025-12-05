package com.ruleengine.persistence.mapper;

import com.ruleengine.domain.rule.Condition;
import com.ruleengine.domain.rule.Rule;
import com.ruleengine.domain.rule.RuleMetadata;
import com.ruleengine.persistence.entity.RuleEntity;

import java.util.ArrayList;
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

        List<Condition> conditions = new ArrayList<>();
        if (entity.getConditions() != null) {
            for (com.ruleengine.persistence.entity.ConditionEntity conditionEntity : entity.getConditions()) {
                Condition condition = ConditionMapper.toDomain(conditionEntity);
                if (condition != null) {
                    conditions.add(condition);
                }
            }
        }

        RuleMetadata metadata = new RuleMetadata(
            entity.getPriority(),
            entity.getActive(),
            entity.getTags() != null ? entity.getTags() : Set.of()
        );

        return new Rule(
            entity.getId(),
            entity.getName(),
            conditions,
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

        // Map conditions
        List<com.ruleengine.persistence.entity.ConditionEntity> conditionEntities = new ArrayList<>();
        for (int i = 0; i < domain.conditions().size(); i++) {
            Condition condition = domain.conditions().get(i);
            com.ruleengine.persistence.entity.ConditionEntity conditionEntity = 
                ConditionMapper.toEntity(condition, entity, i);
            conditionEntities.add(conditionEntity);
        }
        entity.setConditions(conditionEntities);

        return entity;
    }
}

