package com.ruleengine.persistence.repository;

import com.ruleengine.persistence.entity.ExpressionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Expression entities.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
@Repository
public interface ExpressionRepository extends JpaRepository<ExpressionEntity, String> {
    Optional<ExpressionEntity> findById(String id);
    boolean existsById(String id);
}

