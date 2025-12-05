package com.ruleengine.domain.exception;

/**
 * Domain exception thrown when expression evaluation fails.
 * Infrastructure implementations should wrap library-specific exceptions into this domain exception.
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public class ExpressionEvaluationException extends Exception {
    public ExpressionEvaluationException(String message) {
        super(message);
    }

    public ExpressionEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }
}

