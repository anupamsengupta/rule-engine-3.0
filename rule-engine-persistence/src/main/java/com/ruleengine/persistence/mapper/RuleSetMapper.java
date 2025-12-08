package com.ruleengine.persistence.mapper;

import com.ruleengine.domain.factory.EngineType;
import com.ruleengine.domain.rule.Rule;
import com.ruleengine.domain.rule.RuleSet;
import com.ruleengine.persistence.entity.EngineTypeEntity;
import com.ruleengine.persistence.entity.RuleEntity;
import com.ruleengine.persistence.entity.RuleSetEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper for converting between RuleSet domain model and RuleSetEntity.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
public final class RuleSetMapper {

    private RuleSetMapper() {
        // Utility class
    }

    /**
     * Converts a RuleSetEntity to a domain RuleSet.
     */
    public static RuleSet toDomain(RuleSetEntity entity) {
        if (entity == null) {
            return null;
        }

        List<Rule> rules = new ArrayList<>();
        if (entity.getRules() != null) {
            for (RuleEntity ruleEntity : entity.getRules()) {
                Rule rule = RuleMapper.toDomain(ruleEntity);
                if (rule != null) {
                    rules.add(rule);
                }
            }
        }

        return new RuleSet(
            entity.getId(),
            entity.getName(),
            rules,
            entity.getStopOnFirstFailure(),
            toDomainEngineType(entity.getEngineType()),
            entity.getRuleCategory()
        );
    }

    /**
     * Converts a domain RuleSet to a RuleSetEntity.
     */
    public static RuleSetEntity toEntity(RuleSet domain) {
        if (domain == null) {
            return null;
        }

        RuleSetEntity entity = new RuleSetEntity(
            domain.id(),
            domain.name(),
            domain.stopOnFirstFailure(),
            toEntityEngineType(domain.engineType()),
            domain.ruleCategory()
        );

        // Map rules
        List<RuleEntity> ruleEntities = new ArrayList<>();
        for (Rule rule : domain.rules()) {
            RuleEntity ruleEntity = RuleMapper.toEntity(rule);
            if (ruleEntity != null) {
                ruleEntities.add(ruleEntity);
            }
        }
        entity.setRules(ruleEntities);

        return entity;
    }

    /**
     * Converts EngineTypeEntity to domain EngineType.
     */
    private static EngineType toDomainEngineType(EngineTypeEntity entity) {
        if (entity == null) {
            return EngineType.SPEL; // Default
        }
        return switch (entity) {
            case MVEL -> EngineType.MVEL;
            case SPEL -> EngineType.SPEL;
            case JEXL -> EngineType.JEXL;
            case GROOVY -> EngineType.GROOVY;
        };
    }

    /**
     * Converts domain EngineType to EngineTypeEntity.
     */
    private static EngineTypeEntity toEntityEngineType(EngineType domain) {
        if (domain == null) {
            return EngineTypeEntity.SPEL; // Default
        }
        return switch (domain) {
            case MVEL -> EngineTypeEntity.MVEL;
            case SPEL -> EngineTypeEntity.SPEL;
            case JEXL -> EngineTypeEntity.JEXL;
            case GROOVY -> EngineTypeEntity.GROOVY;
        };
    }
}

