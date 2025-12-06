package com.ruleengine.persistence.entity;

/**
 * JPA enum for EngineType persistence.
 * Maps to the domain EngineType enum.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
public enum EngineTypeEntity {
    MVEL,
    SPEL,
    JEXL,
    GROOVY
}

