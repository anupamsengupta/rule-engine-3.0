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
 * Unit tests for SpelExpressionEngine.
 * 
 * Module: rule-engine-infrastructure
 * Layer: Infrastructure
 */
class SpelExpressionEngineTest {

    private final SpelExpressionEngine engine = new SpelExpressionEngine();

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
        EvaluationContext context = EvaluationContext.from(Map.of("a", 10, "b", 5));
        ExpressionEvaluationResult result = engine.evaluate("a + b", context);

        assertThat(result.value()).isEqualTo(15);
        assertThat(result.type()).isEqualTo(AttributeType.NUMBER);
    }

    @Test
    void shouldThrowExceptionForInvalidExpression() {
        EvaluationContext context = EvaluationContext.from(Map.of("x", 10));

        assertThatThrownBy(() -> engine.evaluate("x +", context))
            .isInstanceOf(ExpressionEvaluationException.class);
    }

    @Test
    void shouldSupportSpelEngineType() {
        assertThat(engine.supports(EngineType.SPEL)).isTrue();
        assertThat(engine.supports(EngineType.GROOVY)).isFalse();
    }
}

