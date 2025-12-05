package com.ruleengine.domain.rule;

import com.ruleengine.domain.attribute.Attribute;
import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.ExpressionEvaluationException;
import com.ruleengine.domain.expression.ExpressionEvaluationResult;
import com.ruleengine.domain.factory.EngineType;
import com.ruleengine.domain.operator.ComparisonOperator;
import com.ruleengine.domain.strategy.ExpressionEvaluationStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for Rule domain model.
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
class RuleTest {

    @Test
    void shouldCreateRuleWithValidData() {
        Attribute ageAttr = new Attribute("customer.age", AttributeType.NUMBER);
        Condition condition = new Condition(ageAttr, ComparisonOperator.GTE, 18);
        Rule rule = new Rule("rule-1", "Adult customer", List.of(condition), RuleMetadata.defaults());

        assertThat(rule.id()).isEqualTo("rule-1");
        assertThat(rule.name()).isEqualTo("Adult customer");
        assertThat(rule.conditions()).hasSize(1);
    }

    @Test
    void shouldValidateRuleSuccessfully() throws ExpressionEvaluationException {
        Attribute ageAttr = new Attribute("customer.age", AttributeType.NUMBER);
        Condition condition = new Condition(ageAttr, ComparisonOperator.GTE, 18);
        Rule rule = new Rule("rule-1", "Adult customer", List.of(condition), RuleMetadata.defaults());

        EvaluationContext context = EvaluationContext.from(Map.of("customer.age", 25));
        ExpressionEvaluationStrategy strategy = mock(ExpressionEvaluationStrategy.class);
        try {
            doReturn(ExpressionEvaluationResult.success(true, AttributeType.BOOLEAN))
                .when(strategy).evaluate(anyString(), any(EvaluationContext.class));
        } catch (ExpressionEvaluationException e) {
            // Mock setup - exception won't be thrown
        }

        RuleValidationResult result = rule.validate(context, strategy);

        assertThat(result.passed()).isTrue();
    }

    @Test
    void shouldFailValidationWhenRuleIsNotActive() {
        Attribute ageAttr = new Attribute("customer.age", AttributeType.NUMBER);
        Condition condition = new Condition(ageAttr, ComparisonOperator.GTE, 18);
        RuleMetadata metadata = new RuleMetadata(0, false, null);
        Rule rule = new Rule("rule-1", "Adult customer", List.of(condition), metadata);

        EvaluationContext context = EvaluationContext.from(Map.of("customer.age", 25));
        ExpressionEvaluationStrategy strategy = mock(ExpressionEvaluationStrategy.class);

        RuleValidationResult result = rule.validate(context, strategy);

        assertThat(result.passed()).isFalse();
        assertThat(result.message()).contains("not active");
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        Attribute ageAttr = new Attribute("customer.age", AttributeType.NUMBER);
        Condition condition = new Condition(ageAttr, ComparisonOperator.GTE, 18);

        assertThatThrownBy(() -> new Rule(null, "Adult customer", List.of(condition), RuleMetadata.defaults()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Rule id cannot be null or blank");
    }
}

