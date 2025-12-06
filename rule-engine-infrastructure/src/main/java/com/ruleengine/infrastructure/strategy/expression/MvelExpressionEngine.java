package com.ruleengine.infrastructure.strategy.expression;

import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.ExpressionEvaluationException;
import com.ruleengine.domain.expression.ExpressionEvaluationResult;
import com.ruleengine.domain.factory.EngineType;
import com.ruleengine.domain.strategy.ExpressionEvaluationStrategy;
import org.mvel2.MVEL;

import java.io.Serializable;
import java.util.Map;

/**
 * MVEL (MVFLEX Expression Language) implementation of ExpressionEvaluationStrategy.
 * Bridges attribute values from EvaluationContext into MVEL's evaluation context
 * and wraps MVEL exceptions into domain exceptions.
 * 
 * Module: rule-engine-infrastructure
 * Layer: Infrastructure
 */
public class MvelExpressionEngine implements ExpressionEvaluationStrategy {
    
    @Override
    public ExpressionEvaluationResult evaluate(String expressionString, EvaluationContext context) throws ExpressionEvaluationException {
        try {
            // Get attribute values as a map for MVEL
            Map<String, Object> values = context.getAllValues();
            
            // Rewrite expression to handle variable names with dots using bracket notation
            String rewrittenExpression = rewriteExpressionForVariables(expressionString, values);
            
            // Create a context map with 'map' variable pointing to the values map
            // This allows bracket notation like map['key.with.dots'] to work
            Map<String, Object> mvelContext = new java.util.HashMap<>(values);
            mvelContext.put("map", values);
            
            // Compile the expression for better performance
            Serializable compiledExpression = MVEL.compileExpression(rewrittenExpression);
            
            // Evaluate the expression with the context values
            Object result = MVEL.executeExpression(compiledExpression, mvelContext);
            
            // Determine result type
            AttributeType resultType = inferType(result);
            
            return ExpressionEvaluationResult.success(result, resultType);
        } catch (org.mvel2.CompileException e) {
            throw new ExpressionEvaluationException(
                "MVEL compilation failed: " + e.getMessage(), e
            );
        } catch (Exception e) {
            // Catch all other exceptions including PropertyAccessException
            // (PropertyAccessException extends CompileException, so it's already handled above)
            throw new ExpressionEvaluationException(
                "Unexpected error during MVEL evaluation: " + e.getMessage(), e
            );
        }
    }

    @Override
    public boolean supports(EngineType engineType) {
        return engineType == EngineType.MVEL;
    }

    /**
     * Rewrites expression to handle variable names with dots using MVEL bracket notation.
     * For variable names with dots, uses bracket notation: map['key.with.dots']
     * For simple variable names, uses direct access: variableName
     */
    private String rewriteExpressionForVariables(String expression, Map<String, Object> values) {
        String result = expression;
        // Sort by length (longest first) to avoid partial replacements
        String[] keys = values.keySet().stream()
            .sorted((a, b) -> Integer.compare(b.length(), a.length()))
            .toArray(String[]::new);
        
        for (String key : keys) {
            String escapedKey = escapeRegex(key);
            // For variable names with dots, use bracket notation: map['key.with.dots']
            // For simple variable names, use direct access
            if (key.contains(".")) {
                // Use bracket notation for map access
                result = result.replaceAll("(?<![a-zA-Z0-9_])" + escapedKey + "(?=\\s|$|[^a-zA-Z0-9_.])", "map['" + key + "']");
            } else {
                // Direct variable access for simple names
                result = result.replaceAll("(?<![a-zA-Z0-9_])" + escapedKey + "(?=\\.|\\s|$|[^a-zA-Z0-9_])", key);
            }
        }
        return result;
    }

    /**
     * Escapes special regex characters in a string.
     */
    private String escapeRegex(String str) {
        return str.replaceAll("[\\[\\]{}()*+?.\\\\^$|]", "\\\\$0");
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

