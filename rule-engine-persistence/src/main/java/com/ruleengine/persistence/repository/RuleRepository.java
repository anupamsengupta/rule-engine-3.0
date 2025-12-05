package com.ruleengine.persistence.repository;

import com.ruleengine.persistence.entity.RuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Rule entities.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
@Repository
public interface RuleRepository extends JpaRepository<RuleEntity, String> {
    Optional<RuleEntity> findById(String id);
    
    List<RuleEntity> findByActiveTrue();
    
    List<RuleEntity> findByActiveTrueOrderByPriorityDesc();
    
    @Query("SELECT r FROM RuleEntity r WHERE r.active = true AND :tag MEMBER OF r.tags")
    List<RuleEntity> findByActiveTrueAndTag(String tag);
    
    boolean existsById(String id);
}

