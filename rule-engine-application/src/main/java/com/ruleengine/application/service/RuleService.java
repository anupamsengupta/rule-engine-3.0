package com.ruleengine.application.service;

import com.ruleengine.domain.rule.Rule;
import com.ruleengine.persistence.entity.RuleEntity;
import com.ruleengine.persistence.mapper.RuleMapper;
import com.ruleengine.persistence.repository.RuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Application service for managing Rule entities.
 * Provides CRUD operations for rules.
 *
 * Module: rule-engine-application
 * Layer: Application
 */
@Service
@Transactional
public class RuleService {
    private final RuleRepository ruleRepository;

    public RuleService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    /**
     * Creates a new rule.
     */
    public Rule createRule(Rule rule) {
        if (ruleRepository.existsById(rule.id())) {
            throw new IllegalArgumentException("Rule with id '" + rule.id() + "' already exists");
        }
        RuleEntity entity = RuleMapper.toEntity(rule);
        RuleEntity saved = ruleRepository.save(entity);
        return RuleMapper.toDomain(saved);
    }

    /**
     * Retrieves a rule by id.
     */
    @Transactional(readOnly = true)
    public Optional<Rule> getRuleById(String id) {
        return ruleRepository.findById(id)
                .map(RuleMapper::toDomain);
    }

    /**
     * Retrieves all rules.
     */
    @Transactional(readOnly = true)
    public List<Rule> getAllRules() {
        return ruleRepository.findAll().stream()
                .map(RuleMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all active rules.
     */
    @Transactional(readOnly = true)
    public List<Rule> getActiveRules() {
        return ruleRepository.findByActiveTrue().stream()
                .map(RuleMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves active rules ordered by priority (descending).
     */
    @Transactional(readOnly = true)
    public List<Rule> getActiveRulesOrderedByPriority() {
        return ruleRepository.findByActiveTrueOrderByPriorityDesc().stream()
                .map(RuleMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves active rules by tag.
     */
    @Transactional(readOnly = true)
    public List<Rule> getActiveRulesByTag(String tag) {
        return ruleRepository.findByActiveTrueAndTag(tag).stream()
                .map(RuleMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing rule.
     */
    public Rule updateRule(Rule rule) {
        RuleEntity existing = ruleRepository.findById(rule.id())
                .orElseThrow(() -> new IllegalArgumentException("Rule with id '" + rule.id() + "' not found"));
        
        // Update basic fields
        existing.setName(rule.name());
        existing.setPriority(rule.metadata().priority());
        existing.setActive(rule.metadata().active());
        existing.setTags(rule.metadata().tags());
        
        // Update condition IDs
        existing.getConditionIds().clear();
        existing.getConditionIds().addAll(rule.conditionIds());
        
        RuleEntity saved = ruleRepository.save(existing);
        return RuleMapper.toDomain(saved);
    }

    /**
     * Deletes a rule by id.
     */
    public void deleteRule(String id) {
        if (!ruleRepository.existsById(id)) {
            throw new IllegalArgumentException("Rule with id '" + id + "' not found");
        }
        ruleRepository.deleteById(id);
    }

    /**
     * Checks if a rule exists by id.
     */
    @Transactional(readOnly = true)
    public boolean ruleExists(String id) {
        return ruleRepository.existsById(id);
    }
}

