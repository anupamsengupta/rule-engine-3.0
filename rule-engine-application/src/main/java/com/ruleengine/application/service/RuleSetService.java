package com.ruleengine.application.service;

import com.ruleengine.domain.rule.RuleSet;
import com.ruleengine.persistence.entity.RuleSetEntity;
import com.ruleengine.persistence.mapper.RuleSetMapper;
import com.ruleengine.persistence.repository.RuleSetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Application service for managing RuleSet entities.
 * Provides CRUD operations for rule sets.
 *
 * Module: rule-engine-application
 * Layer: Application
 */
@Service
@Transactional
public class RuleSetService {
    private final RuleSetRepository ruleSetRepository;

    public RuleSetService(RuleSetRepository ruleSetRepository) {
        this.ruleSetRepository = ruleSetRepository;
    }

    /**
     * Creates a new rule set.
     */
    public RuleSet createRuleSet(RuleSet ruleSet) {
        if (ruleSetRepository.existsById(ruleSet.id())) {
            throw new IllegalArgumentException("RuleSet with id '" + ruleSet.id() + "' already exists");
        }
        RuleSetEntity entity = RuleSetMapper.toEntity(ruleSet);
        RuleSetEntity saved = ruleSetRepository.save(entity);
        return RuleSetMapper.toDomain(saved);
    }

    /**
     * Retrieves a rule set by id.
     */
    @Transactional(readOnly = true)
    public Optional<RuleSet> getRuleSetById(String id) {
        return ruleSetRepository.findById(id)
                .map(RuleSetMapper::toDomain);
    }

    /**
     * Retrieves all rule sets.
     */
    @Transactional(readOnly = true)
    public List<RuleSet> getAllRuleSets() {
        return ruleSetRepository.findAll().stream()
                .map(RuleSetMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing rule set.
     */
    public RuleSet updateRuleSet(RuleSet ruleSet) {
        RuleSetEntity existing = ruleSetRepository.findById(ruleSet.id())
                .orElseThrow(() -> new IllegalArgumentException("RuleSet with id '" + ruleSet.id() + "' not found"));
        
        // Map and set new rule set data
        RuleSetEntity updated = RuleSetMapper.toEntity(ruleSet);
        existing.setName(updated.getName());
        existing.setStopOnFirstFailure(updated.getStopOnFirstFailure());
        existing.setEngineType(updated.getEngineType());
        existing.setRules(updated.getRules());
        
        RuleSetEntity saved = ruleSetRepository.save(existing);
        return RuleSetMapper.toDomain(saved);
    }

    /**
     * Deletes a rule set by id.
     */
    public void deleteRuleSet(String id) {
        if (!ruleSetRepository.existsById(id)) {
            throw new IllegalArgumentException("RuleSet with id '" + id + "' not found");
        }
        ruleSetRepository.deleteById(id);
    }

    /**
     * Checks if a rule set exists by id.
     */
    @Transactional(readOnly = true)
    public boolean ruleSetExists(String id) {
        return ruleSetRepository.existsById(id);
    }
}

