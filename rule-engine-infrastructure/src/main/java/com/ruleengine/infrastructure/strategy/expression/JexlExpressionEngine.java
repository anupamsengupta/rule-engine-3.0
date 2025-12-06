package com.ruleengine.infrastructure.strategy.expression;

import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.ExpressionEvaluationException;
import com.ruleengine.domain.expression.ExpressionEvaluationResult;
import com.ruleengine.domain.factory.EngineType;
import com.ruleengine.domain.strategy.ExpressionEvaluationStrategy;
import org.apache.commons.jexl3.*;

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
        // Create JEXL engine with default configuration
        JexlBuilder builder = new JexlBuilder();
        this.jexlEngine = builder.create();
    }

    @Override
    public ExpressionEvaluationResult evaluate(String expressionString, EvaluationContext context) throws ExpressionEvaluationException {
        try {
            // Create JEXL context with attribute values
            JexlContext jexlContext = new MapContext(context.getAllValues());
            
            // Create and compile the expression
            JexlExpression expression = jexlEngine.createExpression(expressionString);
            
            // Evaluate the expression
            Object result = expression.evaluate(jexlContext);
            
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

