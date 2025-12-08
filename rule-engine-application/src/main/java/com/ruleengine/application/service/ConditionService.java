package com.ruleengine.application.service;

import com.ruleengine.domain.rule.Condition;
import com.ruleengine.persistence.entity.ConditionEntity;
import com.ruleengine.persistence.mapper.ConditionMapper;
import com.ruleengine.persistence.repository.ConditionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Application service for managing Condition entities.
 * Provides CRUD operations for conditions.
 *
 * Module: rule-engine-application
 * Layer: Application
 */
@Service
@Transactional
public class ConditionService {
    private final ConditionRepository conditionRepository;

    public ConditionService(ConditionRepository conditionRepository) {
        this.conditionRepository = conditionRepository;
    }

    /**
     * Creates a new condition.
     */
    public Condition createCondition(Condition condition) {
        if (conditionRepository.existsById(condition.id())) {
            throw new IllegalArgumentException("Condition with id '" + condition.id() + "' already exists");
        }
        ConditionEntity entity = ConditionMapper.toEntity(condition);
        ConditionEntity saved = conditionRepository.save(entity);
        return ConditionMapper.toDomain(saved);
    }

    /**
     * Retrieves a condition by id.
     */
    @Transactional(readOnly = true)
    public Optional<Condition> getConditionById(String id) {
        return conditionRepository.findById(id)
                .map(ConditionMapper::toDomain);
    }

    /**
     * Retrieves all conditions.
     */
    @Transactional(readOnly = true)
    public List<Condition> getAllConditions() {
        return conditionRepository.findAll().stream()
                .map(ConditionMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing condition.
     */
    public Condition updateCondition(Condition condition) {
        ConditionEntity existing = conditionRepository.findById(condition.id())
                .orElseThrow(() -> new IllegalArgumentException("Condition with id '" + condition.id() + "' not found"));
        
        // Update fields
        existing.setName(condition.name());
        existing.setLeftAttribute(com.ruleengine.persistence.mapper.AttributeMapper.toEntity(condition.leftAttribute()));
        existing.setOperator(com.ruleengine.persistence.entity.ComparisonOperatorEntity.valueOf(condition.operator().name()));
        
        if (condition.rightAttribute().isPresent()) {
            existing.setRightAttribute(com.ruleengine.persistence.mapper.AttributeMapper.toEntity(condition.rightAttribute().get()));
            existing.setTargetValue(null);
            existing.setTargetValueType(null);
        } else {
            existing.setRightAttribute(null);
            if (condition.targetValue().isPresent()) {
                Object targetValueObj = condition.targetValue().get();
                existing.setTargetValue(targetValueObj != null ? targetValueObj.toString() : null);
                existing.setTargetValueType(targetValueObj != null ? targetValueObj.getClass().getName() : null);
            } else {
                existing.setTargetValue(null);
                existing.setTargetValueType(null);
            }
        }
        
        ConditionEntity saved = conditionRepository.save(existing);
        return ConditionMapper.toDomain(saved);
    }

    /**
     * Deletes a condition by id.
     */
    public void deleteCondition(String id) {
        if (!conditionRepository.existsById(id)) {
            throw new IllegalArgumentException("Condition with id '" + id + "' not found");
        }
        conditionRepository.deleteById(id);
    }

    /**
     * Checks if a condition exists by id.
     */
    @Transactional(readOnly = true)
    public boolean conditionExists(String id) {
        return conditionRepository.existsById(id);
    }

    /**
     * Retrieves multiple conditions by their IDs.
     */
    @Transactional(readOnly = true)
    public List<Condition> getConditionsByIds(List<String> conditionIds) {
        return conditionIds.stream()
                .map(id -> conditionRepository.findById(id)
                        .map(ConditionMapper::toDomain)
                        .orElseThrow(() -> new IllegalArgumentException("Condition with id '" + id + "' not found")))
                .collect(Collectors.toList());
    }
}

