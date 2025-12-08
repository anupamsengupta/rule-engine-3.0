package com.ruleengine.persistence.entity;

import jakarta.persistence.*;

/**
 * JPA entity for Condition persistence.
 * Conditions are standalone entities that can be reused across multiple rules.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
@Entity
@Table(name = "conditions")
public class ConditionEntity {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "left_attribute_code", nullable = false)
    private AttributeEntity leftAttribute;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false)
    private ComparisonOperatorEntity operator;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "right_attribute_code")
    private AttributeEntity rightAttribute;

    @Column(name = "target_value", length = 1000)
    private String targetValue;

    @Column(name = "target_value_type")
    private String targetValueType;

    protected ConditionEntity() {
        // Required by JPA
    }

    public ConditionEntity(String id, String name, AttributeEntity leftAttribute, 
                          ComparisonOperatorEntity operator, AttributeEntity rightAttribute,
                          String targetValue, String targetValueType) {
        this.id = id;
        this.name = name;
        this.leftAttribute = leftAttribute;
        this.operator = operator;
        this.rightAttribute = rightAttribute;
        this.targetValue = targetValue;
        this.targetValueType = targetValueType;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AttributeEntity getLeftAttribute() {
        return leftAttribute;
    }

    public void setLeftAttribute(AttributeEntity leftAttribute) {
        this.leftAttribute = leftAttribute;
    }

    public ComparisonOperatorEntity getOperator() {
        return operator;
    }

    public void setOperator(ComparisonOperatorEntity operator) {
        this.operator = operator;
    }

    public AttributeEntity getRightAttribute() {
        return rightAttribute;
    }

    public void setRightAttribute(AttributeEntity rightAttribute) {
        this.rightAttribute = rightAttribute;
    }

    public String getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(String targetValue) {
        this.targetValue = targetValue;
    }

    public String getTargetValueType() {
        return targetValueType;
    }

    public void setTargetValueType(String targetValueType) {
        this.targetValueType = targetValueType;
    }
}
