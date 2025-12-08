package com.ruleengine.persistence.repository;

import com.ruleengine.persistence.entity.ConditionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for ConditionEntity.
 * Provides CRUD operations for conditions.
 *
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
@Repository
public interface ConditionRepository extends JpaRepository<ConditionEntity, String> {
    
    /**
     * Finds a condition by its ID.
     */
    Optional<ConditionEntity> findById(String id);

    /**
     * Finds all conditions.
     */
    List<ConditionEntity> findAll();

    /**
     * Finds conditions by left attribute code.
     */
    @Query("SELECT c FROM ConditionEntity c WHERE c.leftAttribute.code = :attributeCode")
    List<ConditionEntity> findByLeftAttributeCode(@Param("attributeCode") String attributeCode);

    /**
     * Finds conditions by right attribute code.
     */
    @Query("SELECT c FROM ConditionEntity c WHERE c.rightAttribute.code = :attributeCode")
    List<ConditionEntity> findByRightAttributeCode(@Param("attributeCode") String attributeCode);

    /**
     * Checks if a condition exists by ID.
     */
    boolean existsById(String id);
}

