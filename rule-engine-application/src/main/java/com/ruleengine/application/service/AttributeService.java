package com.ruleengine.application.service;

import com.ruleengine.domain.attribute.Attribute;
import com.ruleengine.persistence.entity.AttributeEntity;
import com.ruleengine.persistence.mapper.AttributeMapper;
import com.ruleengine.persistence.repository.AttributeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Application service for managing Attribute entities.
 * Provides CRUD operations for attributes.
 *
 * Module: rule-engine-application
 * Layer: Application
 */
@Service
@Transactional
public class AttributeService {
    private final AttributeRepository attributeRepository;

    public AttributeService(AttributeRepository attributeRepository) {
        this.attributeRepository = attributeRepository;
    }

    /**
     * Creates a new attribute.
     */
    public Attribute createAttribute(Attribute attribute) {
        if (attributeRepository.existsByCode(attribute.code())) {
            throw new IllegalArgumentException("Attribute with code '" + attribute.code() + "' already exists");
        }
        AttributeEntity entity = AttributeMapper.toEntity(attribute);
        AttributeEntity saved = attributeRepository.save(entity);
        return AttributeMapper.toDomain(saved);
    }

    /**
     * Retrieves an attribute by code.
     */
    @Transactional(readOnly = true)
    public Optional<Attribute> getAttributeByCode(String code) {
        return attributeRepository.findByCode(code)
                .map(AttributeMapper::toDomain);
    }

    /**
     * Retrieves all attributes.
     */
    @Transactional(readOnly = true)
    public List<Attribute> getAllAttributes() {
        return attributeRepository.findAll().stream()
                .map(AttributeMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing attribute.
     */
    public Attribute updateAttribute(Attribute attribute) {
        AttributeEntity existing = attributeRepository.findByCode(attribute.code())
                .orElseThrow(() -> new IllegalArgumentException("Attribute with code '" + attribute.code() + "' not found"));
        
        // Update fields
        existing.setPath(attribute.path());
        existing.setType(com.ruleengine.persistence.entity.AttributeTypeEntity.valueOf(attribute.type().name()));
        existing.setDescription(attribute.description().orElse(null));
        
        // Update constraints
        if (attribute.constraints().isPresent()) {
            existing.setConstraints(attribute.constraints().get().entrySet().stream()
                    .collect(java.util.stream.Collectors.toMap(
                            java.util.Map.Entry::getKey,
                            entry -> entry.getValue() != null ? entry.getValue().toString() : null
                    )));
        } else {
            existing.setConstraints(null);
        }
        
        AttributeEntity saved = attributeRepository.save(existing);
        return AttributeMapper.toDomain(saved);
    }

    /**
     * Deletes an attribute by code.
     */
    public void deleteAttribute(String code) {
        if (!attributeRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Attribute with code '" + code + "' not found");
        }
        attributeRepository.deleteById(code);
    }

    /**
     * Checks if an attribute exists by code.
     */
    @Transactional(readOnly = true)
    public boolean attributeExists(String code) {
        return attributeRepository.existsByCode(code);
    }
}

