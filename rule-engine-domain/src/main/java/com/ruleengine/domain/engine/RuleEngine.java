package com.ruleengine.domain.engine;

import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.rule.RuleValidationResult;

import java.util.List;

/**
 * Interface for rule validation engine.
 * Provides operations to validate individual rules or rule sets against evaluation contexts.
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public interface RuleEngine {
    /**
     * Validates a single rule against the given evaluation context.
     *
     * @param ruleId  The identifier of the rule to validate
     * @param context The evaluation context containing attribute values
     * @return RuleValidationResult indicating whether the rule passed
     * @throws com.ruleengine.domain.exception.RuleEvaluationException if validation fails
     */
    RuleValidationResult validateRule(String ruleId, EvaluationContext context);

    /**
     * Validates a set of rules against the given evaluation context.
     *
     * @param ruleSetId The identifier of the rule set to validate
     * @param context   The evaluation context containing attribute values
     * @return List of RuleValidationResult, one for each rule in the set
     * @throws com.ruleengine.domain.exception.RuleEvaluationException if validation fails
     */
    List<RuleValidationResult> validateRules(String ruleSetId, EvaluationContext context);
}

