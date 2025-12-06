package com.ruleengine.infrastructure.strategy.expression;

import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.ExpressionEvaluationException;
import com.ruleengine.domain.expression.ExpressionEvaluationResult;
import com.ruleengine.domain.factory.EngineType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JexlExpressionEngine.
 * 
 * Module: rule-engine-infrastructure
 * Layer: Infrastructure
 */
class JexlExpressionEngineTest {

    private final JexlExpressionEngine engine = new JexlExpressionEngine();

    @Test
    void shouldEvaluateSimpleExpression() throws ExpressionEvaluationException {
        EvaluationContext context = EvaluationContext.from(Map.of("x", 10, "y", 5));
        ExpressionEvaluationResult result = engine.evaluate("x > y", context);

        assertThat(result.value()).isEqualTo(true);
        assertThat(result.type()).isEqualTo(AttributeType.BOOLEAN);
        assertThat(result.error()).isEmpty();
    }

    @Test
    void shouldEvaluateArithmeticExpression() throws ExpressionEvaluationException {
        EvaluationContext context = EvaluationContext.from(Map.of("a_var", 10, "b_var", 5));
        ExpressionEvaluationResult result = engine.evaluate("a_var + b_var", context);

        assertThat(result.value()).isEqualTo(15);
        assertThat(result.type()).isEqualTo(AttributeType.NUMBER);
    }

    @Test
    void shouldEvaluateComplexExpression() throws ExpressionEvaluationException {
        EvaluationContext context = EvaluationContext.from(Map.of("x", 10, "y", 5, "z", 3));
        ExpressionEvaluationResult result = engine.evaluate("x > y && y > z", context);

        assertThat(result.value()).isEqualTo(true);
        assertThat(result.type()).isEqualTo(AttributeType.BOOLEAN);
    }

    @Test
    void shouldEvaluateExpressionWithVariableNamesContainingDots() throws ExpressionEvaluationException {
        EvaluationContext context = EvaluationContext.from(Map.of("customer.age", 25, "minimum.age", 18));
        ExpressionEvaluationResult result = engine.evaluate("customer.age >= minimum.age", context);

        assertThat(result.value()).isEqualTo(true);
        assertThat(result.type()).isEqualTo(AttributeType.BOOLEAN);
    }

    @Test
    void shouldThrowExceptionForInvalidExpression() {
        EvaluationContext context = EvaluationContext.from(Map.of("x", 10));

        assertThatThrownBy(() -> engine.evaluate("x +", context))
            .isInstanceOf(ExpressionEvaluationException.class);
    }

    @Test
    void shouldSupportJexlEngineType() {
        assertThat(engine.supports(EngineType.JEXL)).isTrue();
        assertThat(engine.supports(EngineType.SPEL)).isFalse();
        assertThat(engine.supports(EngineType.MVEL)).isFalse();
        assertThat(engine.supports(EngineType.GROOVY)).isFalse();
    }
}

