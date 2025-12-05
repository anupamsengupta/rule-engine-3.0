package com.ruleengine.persistence.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for RuleSet persistence.
 * Maps to the domain RuleSet model.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
@Entity
@Table(name = "rule_sets")
public class RuleSetEntity {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "rule_set_rules",
        joinColumns = @JoinColumn(name = "rule_set_id"),
        inverseJoinColumns = @JoinColumn(name = "rule_id")
    )
    @OrderColumn(name = "rule_order")
    private List<RuleEntity> rules = new ArrayList<>();

    @Column(name = "stop_on_first_failure", nullable = false)
    private Boolean stopOnFirstFailure;

    protected RuleSetEntity() {
        // Required by JPA
    }

    public RuleSetEntity(String id, String name, Boolean stopOnFirstFailure) {
        this.id = id;
        this.name = name;
        this.stopOnFirstFailure = stopOnFirstFailure;
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

    public List<RuleEntity> getRules() {
        return rules;
    }

    public void setRules(List<RuleEntity> rules) {
        this.rules = rules;
    }

    public Boolean getStopOnFirstFailure() {
        return stopOnFirstFailure;
    }

    public void setStopOnFirstFailure(Boolean stopOnFirstFailure) {
        this.stopOnFirstFailure = stopOnFirstFailure;
    }
}

