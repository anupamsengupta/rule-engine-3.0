package com.ruleengine.persistence.entity;

import jakarta.persistence.*;

/**
 * JPA entity for Expression persistence.
 * Maps to the domain Expression model.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
@Entity
@Table(name = "expressions")
public class ExpressionEntity {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "expression_string", nullable = false, length = 5000)
    private String expressionString;

    @Column(name = "description", length = 1000)
    private String description;

    protected ExpressionEntity() {
        // Required by JPA
    }

    public ExpressionEntity(String id, String expressionString, String description) {
        this.id = id;
        this.expressionString = expressionString;
        this.description = description;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExpressionString() {
        return expressionString;
    }

    public void setExpressionString(String expressionString) {
        this.expressionString = expressionString;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

