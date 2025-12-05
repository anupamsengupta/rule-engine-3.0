package com.ruleengine.infrastructure.strategy.script;

import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.ExpressionEvaluationException;
import com.ruleengine.domain.expression.ExpressionEvaluationResult;
import com.ruleengine.domain.factory.EngineType;
import com.ruleengine.domain.strategy.ScriptEvaluationStrategy;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.math.BigDecimal;

/**
 * Groovy scripting implementation of ScriptEvaluationStrategy.
 * Executes Groovy scripts using the attribute evaluation context,
 * allowing for complex logic and helper methods.
 * 
 * Module: rule-engine-infrastructure
 * Layer: Infrastructure
 */
public class GroovyScriptEngine implements ScriptEvaluationStrategy {
    private final GroovyShell groovyShell;

    public GroovyScriptEngine() {
        this.groovyShell = new GroovyShell();
    }

    @Override
    public ExpressionEvaluationResult evaluate(String script, EvaluationContext context) throws ExpressionEvaluationException {
        try {
            // Create Groovy binding and populate with attribute values
            Binding binding = new Binding();
            context.getAllValues().forEach(binding::setVariable);

            // Create a new shell with the binding
            GroovyShell shell = new GroovyShell(binding);

            // Evaluate the script
            Object result = shell.evaluate(script);

            // Determine result type
            AttributeType resultType = inferType(result);

            return ExpressionEvaluationResult.success(result, resultType);
        } catch (groovy.lang.MissingPropertyException e) {
            throw new ExpressionEvaluationException(
                "Groovy script error - missing property: " + e.getMessage(), e
            );
        } catch (groovy.lang.GroovyRuntimeException e) {
            throw new ExpressionEvaluationException(
                "Groovy runtime error: " + e.getMessage(), e
            );
        } catch (Exception e) {
            throw new ExpressionEvaluationException(
                "Unexpected error during Groovy script evaluation: " + e.getMessage(), e
            );
        }
    }

    @Override
    public boolean supports(EngineType engineType) {
        return engineType == EngineType.GROOVY;
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
            if (value instanceof Double || value instanceof Float || value instanceof BigDecimal) {
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

