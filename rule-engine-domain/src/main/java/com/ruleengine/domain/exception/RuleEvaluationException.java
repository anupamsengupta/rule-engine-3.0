package com.ruleengine.domain.exception;

/**
 * Domain exception thrown when rule evaluation fails.
 * This is a checked exception to ensure callers handle rule evaluation errors appropriately.
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public class RuleEvaluationException extends Exception {
    public RuleEvaluationException(String message) {
        super(message);
    }

    public RuleEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }
}

