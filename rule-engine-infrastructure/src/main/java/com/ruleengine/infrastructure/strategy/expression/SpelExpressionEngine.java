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
            // Parse the expression
            Expression expression = parser.parseExpression(expressionString);

            // Create SPEL evaluation context with attribute values as root object
            // This allows variables to be accessed directly by name (e.g., "customer.age")
            StandardEvaluationContext spelContext = new StandardEvaluationContext(context.getAllValues());

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

