package com.ruleengine.domain.command;

import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.ExpressionEvaluationException;
import com.ruleengine.domain.expression.Expression;
import com.ruleengine.domain.expression.ExpressionEvaluationResult;
import com.ruleengine.domain.strategy.ExpressionEvaluationStrategy;

/**
 * Command encapsulating an expression evaluation operation.
 * Follows the Command pattern to encapsulate the evaluation action as a single-responsibility unit.
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public final class EvaluateExpressionCommand {
    private final Expression expression;
    private final EvaluationContext context;
    private final ExpressionEvaluationStrategy strategy;

    public EvaluateExpressionCommand(
            Expression expression,
            EvaluationContext context,
            ExpressionEvaluationStrategy strategy
    ) {
        if (expression == null) {
            throw new IllegalArgumentException("Expression cannot be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("Evaluation context cannot be null");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        this.expression = expression;
        this.context = context;
        this.strategy = strategy;
    }

    /**
     * Executes the expression evaluation command.
     *
     * @return ExpressionEvaluationResult containing the evaluated value or error
     */
    public ExpressionEvaluationResult execute() throws ExpressionEvaluationException {
        return expression.evaluate(context, strategy);
    }
}

