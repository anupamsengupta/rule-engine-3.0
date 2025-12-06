package com.ruleengine.infrastructure.strategy.expression;

import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.ExpressionEvaluationException;
import com.ruleengine.domain.expression.ExpressionEvaluationResult;
import com.ruleengine.domain.factory.EngineType;
import com.ruleengine.domain.strategy.ExpressionEvaluationStrategy;
import org.apache.commons.jexl3.*;
import org.apache.commons.jexl3.introspection.JexlPermissions;

import java.util.Map;

/**
 * JEXL (Jakarta Expression Language) implementation of ExpressionEvaluationStrategy.
 * Bridges attribute values from EvaluationContext into JEXL's evaluation context
 * and wraps JEXL exceptions into domain exceptions.
 * 
 * Module: rule-engine-infrastructure
 * Layer: Infrastructure
 */
public class JexlExpressionEngine implements ExpressionEvaluationStrategy {
    private final JexlEngine jexlEngine;

    public JexlExpressionEngine() {
        // Create JEXL engine with configuration that supports method calls and static method access
        // This allows JEXL to call methods on objects (like record accessors) and static methods
        JexlBuilder builder = new JexlBuilder();
        // Enable static method access
        builder.cache(512);
        this.jexlEngine = builder
                .permissions(JexlPermissions.UNRESTRICTED)
                .silent(false)
                .create();
    }

    @Override
    public ExpressionEvaluationResult evaluate(String expressionString, EvaluationContext context) throws ExpressionEvaluationException {
        try {
            // Create JEXL context with attribute values
            JexlContext jexlContext = new MapContext(context.getAllValues());

            // Check if expression contains multiple statements (semicolons or control structures)
            // JEXL scripts can have: for loops (with ':' or 'in'), foreach, if statements, semicolons
            boolean isScript = expressionString.contains(";") ||
                    expressionString.contains("for ") ||
                    expressionString.contains("foreach ") ||
                    expressionString.contains("if ") ||
                    expressionString.contains(" while ");
            Object result;
            if (isScript) {
                // Try to create and evaluate as script
                try {
                    JexlScript jexlScript = jexlEngine.createScript(expressionString);
                    result = jexlScript.execute(jexlContext);
                } catch (Exception scriptException) {
                    // If script creation fails, try as expression (for backward compatibility)
                    try {
                        JexlExpression jexlExpression = jexlEngine.createExpression(expressionString);
                        result = jexlExpression.evaluate(jexlContext);
                    } catch (Exception exprException) {
                        // Re-throw the original script exception as it's more informative
                        throw scriptException;
                    }
                }
            } else {
                // Create and evaluate as expression
                JexlExpression jexlExpression = jexlEngine.createExpression(expressionString);
                result = jexlExpression.evaluate(jexlContext);
            }

            // Determine result type
            AttributeType resultType = inferType(result);
            
            return ExpressionEvaluationResult.success(result, resultType);
        } catch (JexlException e) {
            throw new ExpressionEvaluationException(
                "JEXL evaluation failed: " + e.getMessage(), e
            );
        } catch (Exception e) {
            throw new ExpressionEvaluationException(
                "Unexpected error during JEXL evaluation: " + e.getMessage(), e
            );
        }
    }

    @Override
    public boolean supports(EngineType engineType) {
        return engineType == EngineType.JEXL;
    }

    /**
     * Infers the AttributeType from a result value.
     */
    private AttributeType inferType(Object value) {
        if (value == null) {
            return AttributeType.STRING;
        }
        if (value instanceof Boolean) {
            return AttributeType.BOOLEAN;
        }
        if (value instanceof Number) {
            if (value instanceof Double || value instanceof Float) {
                return AttributeType.DECIMAL;
            }
            return AttributeType.NUMBER;
        }
        if (value instanceof java.time.LocalDate) {
            return AttributeType.DATE;
        }
        if (value instanceof java.time.LocalDateTime || value instanceof java.util.Date) {
            return AttributeType.DATETIME;
        }
        return AttributeType.STRING;
    }
}

