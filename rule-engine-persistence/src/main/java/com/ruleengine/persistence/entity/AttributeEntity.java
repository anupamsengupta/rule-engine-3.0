package com.ruleengine.persistence.entity;

import jakarta.persistence.*;

import java.util.Map;

/**
 * JPA entity for Attribute persistence.
 * Maps to the domain Attribute model.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
@Entity
@Table(name = "attributes")
public class AttributeEntity {
    @Id
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AttributeTypeEntity type;

    @Column(name = "description", length = 1000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "attribute_constraints", joinColumns = @JoinColumn(name = "attribute_code"))
    @MapKeyColumn(name = "constraint_key")
    @Column(name = "constraint_value")
    private Map<String, String> constraints;

    protected AttributeEntity() {
        // Required by JPA
    }

    public AttributeEntity(String code, AttributeTypeEntity type, String description, Map<String, String> constraints) {
        this.code = code;
        this.type = type;
        this.description = description;
        this.constraints = constraints;
    }

    // Getters and setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public AttributeTypeEntity getType() {
        return type;
    }

    public void setType(AttributeTypeEntity type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getConstraints() {
        return constraints;
    }

    public void setConstraints(Map<String, String> constraints) {
        this.constraints = constraints;
    }
}

