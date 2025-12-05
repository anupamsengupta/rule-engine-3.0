package com.ruleengine.persistence.entity;

/**
 * Enum for attribute types in persistence layer.
 * Mirrors the domain AttributeType enum.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
public enum AttributeTypeEntity {
    STRING,
    NUMBER,
    BOOLEAN,
    DATE,
    DATETIME,
    DECIMAL
}

