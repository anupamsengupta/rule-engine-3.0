package com.ruleengine.domain.rule;

import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.strategy.ExpressionEvaluationStrategy;

import java.util.List;

/**
 * Domain model representing a rule that can be validated against an evaluation context.
 * Rules reference conditions by ID, allowing conditions to be reused across multiple rules.
 * Rules are immutable and delegate actual validation logic to strategies.
 *
 * @param id          Unique identifier for the rule
 * @param name        Human-readable name
 * @param conditionIds  List of condition IDs that must be satisfied
 * @param metadata    Optional metadata (priority, active flag, tags)
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public record Rule(
        String id,
        String name,
        List<String> conditionIds,
        RuleMetadata metadata
) {
    public Rule {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Rule id cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Rule name cannot be null or blank");
        }
        if (conditionIds == null || conditionIds.isEmpty()) {
            throw new IllegalArgumentException("Rule must have at least one condition");
        }
        if (metadata == null) {
            metadata = RuleMetadata.defaults();
        }
    }

    /**
     * Validates this rule against the given evaluation context using the provided conditions.
     * The actual validation logic is delegated to a strategy.
     *
     * @param context   The evaluation context containing attribute values
     * @param strategy  The strategy to use for expression evaluation
     * @param conditions The list of conditions to evaluate (resolved from conditionIds)
     * @return RuleValidationResult indicating whether the rule passed
     */
    public RuleValidationResult validate(EvaluationContext context, ExpressionEvaluationStrategy strategy, 
                                        List<Condition> conditions) {
        if (!metadata.active()) {
            return RuleValidationResult.failure("Rule is not active");
        }

        try {
            // Build expression from conditions and evaluate using strategy
            String expression = buildExpression(conditions);
            var expressionResult = strategy.evaluate(expression, context);

            if (expressionResult.error().isPresent()) {
                return RuleValidationResult.failure(
                    "Expression evaluation failed: " + expressionResult.error().get()
                );
            }

            // Convert expression result to boolean
            boolean passed = convertToBoolean(expressionResult.value());

            if (passed) {
                return RuleValidationResult.success("Rule validation passed");
            } else {
                return RuleValidationResult.failure("Rule validation failed: conditions not satisfied");
            }
        } catch (Exception e) {
            return RuleValidationResult.failure("Rule validation error: " + e.getMessage());
        }
    }

    /**
     * Builds an expression string from the conditions.
     */
    private String buildExpression(List<Condition> conditions) {
        var builder = new StringBuilder();
        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) {
                builder.append(" AND ");
            }
            Condition condition = conditions.get(i);
            
            builder.append(condition.leftAttribute().code());
            builder.append(" ").append(condition.operator().getSymbol()).append(" ");
            
            if (condition.rightAttribute().isPresent()) {
                // Attribute vs Attribute
                builder.append(condition.rightAttribute().get().code());
            } else if (condition.targetValue().isPresent()) {
                // Attribute vs Value
                builder.append(formatValue(condition.targetValue().get()));
            }
        }
        return builder.toString();
    }

    /**
     * Formats a value for inclusion in an expression string.
     */
    private String formatValue(Object value) {
        if (value instanceof String) {
            return "'" + value + "'";
        }
        return String.valueOf(value);
    }

    /**
     * Converts an expression evaluation result to a boolean.
     */
    private boolean convertToBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0.0;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return value != null;
    }
}
