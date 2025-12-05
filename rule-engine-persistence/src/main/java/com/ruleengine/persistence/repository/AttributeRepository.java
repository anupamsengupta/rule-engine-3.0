package com.ruleengine.persistence.repository;

import com.ruleengine.persistence.entity.AttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Attribute entities.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
@Repository
public interface AttributeRepository extends JpaRepository<AttributeEntity, String> {
    Optional<AttributeEntity> findByCode(String code);
    boolean existsByCode(String code);
}

