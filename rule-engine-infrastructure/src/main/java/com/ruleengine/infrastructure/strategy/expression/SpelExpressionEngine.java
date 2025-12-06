package com.ruleengine.infrastructure.strategy.expression;

import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.ExpressionEvaluationException;
import com.ruleengine.domain.expression.ExpressionEvaluationResult;
import com.ruleengine.domain.factory.EngineType;
import com.ruleengine.domain.strategy.ExpressionEvaluationStrategy;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * SPEL (Spring Expression Language) implementation of ExpressionEvaluationStrategy.
 * Bridges attribute values from EvaluationContext into SPEL's evaluation context
 * and wraps SPEL exceptions into domain exceptions.
 * 
 * Module: rule-engine-infrastructure
 * Layer: Infrastructure
 */
public class SpelExpressionEngine implements ExpressionEvaluationStrategy {
    private final ExpressionParser parser;

    public SpelExpressionEngine() {
        this.parser = new SpelExpressionParser();
    }

    @Override
    public ExpressionEvaluationResult evaluate(String expressionString, EvaluationContext context) throws ExpressionEvaluationException {
        try {
            Map<String, Object> values = context.getAllValues();
            StandardEvaluationContext spelContext = new StandardEvaluationContext();
            
            // Set the map as root object to allow bracket notation access for variables with dots
            spelContext.setRootObject(values);
            
            // Also set variables with # prefix for explicit variable access
            // This allows both #variableName and direct property access
            values.forEach(spelContext::setVariable);
            
            // If there's a single variable and the expression uses it as root object (e.g., "root.property"),
            // also set it as the root object for direct property access
            if (values.size() == 1) {
                String key = values.keySet().iterator().next();
                Object value = values.get(key);
                // Check if expression uses the variable with property access (e.g., "root.property")
                if (expressionString.contains(key + ".")) {
                    spelContext.setRootObject(value);
                }
            }
            
            // Rewrite expression to use #variable syntax
            // For variable names with dots, use bracket notation if needed
            String rewrittenExpression = rewriteExpressionForVariables(expressionString, values);
            
            // Parse the rewritten expression
            Expression expression = parser.parseExpression(rewrittenExpression);

            // Evaluate the expression
            Object result = expression.getValue(spelContext);

            // Determine result type
            AttributeType resultType = inferType(result);

            return ExpressionEvaluationResult.success(result, resultType);
        } catch (org.springframework.expression.EvaluationException e) {
            throw new ExpressionEvaluationException(
                "SPEL evaluation failed: " + e.getMessage(), e
            );
        } catch (org.springframework.expression.ParseException e) {
            throw new ExpressionEvaluationException(
                "SPEL parsing failed: " + e.getMessage(), e
            );
        } catch (Exception e) {
            throw new ExpressionEvaluationException(
                "Unexpected error during SPEL evaluation: " + e.getMessage(), e
            );
        }
    }

    @Override
    public boolean supports(EngineType engineType) {
        return engineType == EngineType.SPEL;
    }

    /**
     * Rewrites expression to use SPEL variable syntax (#variableName).
     * Replaces variable names with #variableName format, but preserves property access.
     * Skips variables that already have # prefix to avoid double-prefixing.
     * For variable names with dots, uses bracket notation to access map entries via root.
     * For example: "root.cartTotalAmount" becomes "#root.cartTotalAmount"
     *              "x > y" becomes "#x > #y"
     *              "customer.age >= 18" becomes "#root['customer.age'] >= 18" (using root map)
     *              or "#customer.age >= 18" if customer.age is a direct variable (SPEL handles dots in variable names)
     */
    private String rewriteExpressionForVariables(String expression, Map<String, Object> values) {
        String result = expression;
        // Sort by length (longest first) to avoid partial replacements
        String[] keys = values.keySet().stream()
            .sorted((a, b) -> Integer.compare(b.length(), a.length()))
            .toArray(String[]::new);
        
        for (String key : keys) {
            String escapedKey = escapeRegex(key);
            // Skip if variable already has # prefix (avoid double-prefixing)
            // Replace variable names with #variableName, but only if not already prefixed
            // Pattern: word boundary, key, followed by space, end, or operator (but not dot for property access)
            // But NOT if preceded by #
            // For variable names with dots, use bracket notation via root map to avoid SPEL treating dot as property access
            // For simple variable names, use direct variable access with # prefix
            if (key.contains(".")) {
                // For variables with dots, use bracket notation: #root['key.with.dots']
                result = result.replaceAll("(?<!#)\\b" + escapedKey + "(?=\\s|$|[^a-zA-Z0-9_.])", "#root['" + key + "']");
            } else {
                // For simple variable names without dots, use direct variable access: #variableName
                result = result.replaceAll("(?<!#)\\b" + escapedKey + "(?=\\.|\\s|$|[^a-zA-Z0-9_])", "#" + key);
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

