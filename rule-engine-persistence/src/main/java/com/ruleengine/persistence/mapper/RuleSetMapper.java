package com.ruleengine.persistence.mapper;

import com.ruleengine.domain.rule.Rule;
import com.ruleengine.domain.rule.RuleSet;
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
            entity.getStopOnFirstFailure()
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
            domain.stopOnFirstFailure()
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
}

