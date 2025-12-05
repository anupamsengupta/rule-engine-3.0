package com.ruleengine.persistence.entity;

import jakarta.persistence.*;

/**
 * JPA entity for Condition persistence.
 * Represents a single condition in a rule.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
@Entity
@Table(name = "conditions")
public class ConditionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private RuleEntity rule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_code", nullable = false)
    private AttributeEntity attribute;

    @Enumerated(EnumType.STRING)
    @Column(name = "operator", nullable = false)
    private ComparisonOperatorEntity operator;

    @Column(name = "target_value", nullable = false, length = 1000)
    private String targetValue;

    @Column(name = "target_value_type", nullable = false)
    private String targetValueType;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    protected ConditionEntity() {
        // Required by JPA
    }

    public ConditionEntity(RuleEntity rule, AttributeEntity attribute, ComparisonOperatorEntity operator, 
                          String targetValue, String targetValueType, Integer sequenceOrder) {
        this.rule = rule;
        this.attribute = attribute;
        this.operator = operator;
        this.targetValue = targetValue;
        this.targetValueType = targetValueType;
        this.sequenceOrder = sequenceOrder;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RuleEntity getRule() {
        return rule;
    }

    public void setRule(RuleEntity rule) {
        this.rule = rule;
    }

    public AttributeEntity getAttribute() {
        return attribute;
    }

    public void setAttribute(AttributeEntity attribute) {
        this.attribute = attribute;
    }

    public ComparisonOperatorEntity getOperator() {
        return operator;
    }

    public void setOperator(ComparisonOperatorEntity operator) {
        this.operator = operator;
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

    public Integer getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }
}

