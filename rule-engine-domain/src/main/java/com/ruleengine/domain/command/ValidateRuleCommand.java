package com.ruleengine.domain.command;

import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.rule.Rule;
import com.ruleengine.domain.rule.RuleValidationResult;
import com.ruleengine.domain.strategy.ExpressionEvaluationStrategy;

/**
 * Command encapsulating a rule validation operation.
 * Follows the Command pattern to encapsulate the validation action as a single-responsibility unit.
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public final class ValidateRuleCommand {
    private final Rule rule;
    private final EvaluationContext context;
    private final ExpressionEvaluationStrategy strategy;

    public ValidateRuleCommand(Rule rule, EvaluationContext context, ExpressionEvaluationStrategy strategy) {
        if (rule == null) {
            throw new IllegalArgumentException("Rule cannot be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("Evaluation context cannot be null");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        this.rule = rule;
        this.context = context;
        this.strategy = strategy;
    }

    /**
     * Executes the rule validation command.
     *
     * @return RuleValidationResult indicating whether the rule passed
     */
    public RuleValidationResult execute() {
        return rule.validate(context, strategy);
    }
}

