package com.ruleengine.persistence.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * JPA entity for Rule persistence.
 * Maps to the domain Rule model.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
@Entity
@Table(name = "rules")
public class RuleEntity {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @OneToMany(mappedBy = "rule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("sequenceOrder ASC")
    private List<ConditionEntity> conditions = new ArrayList<>();

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @ElementCollection
    @CollectionTable(name = "rule_tags", joinColumns = @JoinColumn(name = "rule_id"))
    @Column(name = "tag")
    private Set<String> tags;

    protected RuleEntity() {
        // Required by JPA
    }

    public RuleEntity(String id, String name, Integer priority, Boolean active, Set<String> tags) {
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.active = active;
        this.tags = tags;
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

    public List<ConditionEntity> getConditions() {
        return conditions;
    }

    public void setConditions(List<ConditionEntity> conditions) {
        this.conditions = conditions;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
}

