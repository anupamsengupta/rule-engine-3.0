package com.ruleengine.domain.factory;

/**
 * Enum-based factory for engine types.
 * Used to identify and select the appropriate evaluation strategy implementation.
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public enum EngineType {
    MVEL,
    SPEL,
    JEXL,
    GROOVY
}

