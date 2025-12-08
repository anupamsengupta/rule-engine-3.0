package com.ruleengine.persistence.repository;

import com.ruleengine.persistence.entity.RuleSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for RuleSet entities.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
@Repository
public interface RuleSetRepository extends JpaRepository<RuleSetEntity, String> {
    Optional<RuleSetEntity> findById(String id);
    boolean existsById(String id);
    List<RuleSetEntity> findByRuleCategory(String ruleCategory);
}

