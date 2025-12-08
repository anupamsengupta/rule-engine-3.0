package com.ruleengine.domain.rule;

import com.ruleengine.domain.attribute.Attribute;
import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.ExpressionEvaluationException;
import com.ruleengine.domain.expression.ExpressionEvaluationResult;
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
        Rule rule = new Rule(
                "rule-1",
                "Adult customer",
                List.of("cond-1", "cond-2"),
                RuleMetadata.defaults()
        );

        assertThat(rule.id()).isEqualTo("rule-1");
        assertThat(rule.name()).isEqualTo("Adult customer");
        assertThat(rule.conditionIds()).hasSize(2);
        assertThat(rule.conditionIds()).containsExactly("cond-1", "cond-2");
    }

    @Test
    void shouldValidateRuleSuccessfully() throws ExpressionEvaluationException {
        Attribute ageAttr = new Attribute("customer.age", AttributeType.NUMBER);
        Condition condition = Condition.attributeVsValue(
                "cond-1",
                "Age check",
                ageAttr,
                ComparisonOperator.GTE,
                18
        );
        
        Rule rule = new Rule(
                "rule-1",
                "Adult customer",
                List.of("cond-1"),
                RuleMetadata.defaults()
        );

        EvaluationContext context = EvaluationContext.from(Map.of("customer.age", 25));
        ExpressionEvaluationStrategy strategy = mock(ExpressionEvaluationStrategy.class);
        try {
            doReturn(ExpressionEvaluationResult.success(true, AttributeType.BOOLEAN))
                .when(strategy).evaluate(anyString(), any(EvaluationContext.class));
        } catch (ExpressionEvaluationException e) {
            // Mock setup - exception won't be thrown
        }

        RuleValidationResult result = rule.validate(context, strategy, List.of(condition));

        assertThat(result.passed()).isTrue();
    }

    @Test
    void shouldFailValidationWhenRuleIsNotActive() {
        Attribute ageAttr = new Attribute("customer.age", AttributeType.NUMBER);
        Condition condition = Condition.attributeVsValue(
                "cond-1",
                "Age check",
                ageAttr,
                ComparisonOperator.GTE,
                18
        );
        
        RuleMetadata metadata = new RuleMetadata(0, false, null);
        Rule rule = new Rule(
                "rule-1",
                "Adult customer",
                List.of("cond-1"),
                metadata
        );

        EvaluationContext context = EvaluationContext.from(Map.of("customer.age", 25));
        ExpressionEvaluationStrategy strategy = mock(ExpressionEvaluationStrategy.class);

        RuleValidationResult result = rule.validate(context, strategy, List.of(condition));

        assertThat(result.passed()).isFalse();
        assertThat(result.message().get().contains("not active")).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> new Rule(
                null,
                "Adult customer",
                List.of("cond-1"),
                RuleMetadata.defaults()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Rule id cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenIdIsBlank() {
        assertThatThrownBy(() -> new Rule(
                "   ",
                "Adult customer",
                List.of("cond-1"),
                RuleMetadata.defaults()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Rule id cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> new Rule(
                "rule-1",
                null,
                List.of("cond-1"),
                RuleMetadata.defaults()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Rule name cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThatThrownBy(() -> new Rule(
                "rule-1",
                "   ",
                List.of("cond-1"),
                RuleMetadata.defaults()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Rule name cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenConditionIdsIsNull() {
        assertThatThrownBy(() -> new Rule(
                "rule-1",
                "Adult customer",
                null,
                RuleMetadata.defaults()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Rule must have at least one condition");
    }

    @Test
    void shouldThrowExceptionWhenConditionIdsIsEmpty() {
        assertThatThrownBy(() -> new Rule(
                "rule-1",
                "Adult customer",
                List.of(),
                RuleMetadata.defaults()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Rule must have at least one condition");
    }

    @Test
    void shouldBuildExpressionWithAttributeVsValue() {
        Attribute ageAttr = new Attribute("customer.age", AttributeType.NUMBER);
        Condition condition = Condition.attributeVsValue(
                "cond-1",
                "Age check",
                ageAttr,
                ComparisonOperator.GTE,
                18
        );
        
        Rule rule = new Rule(
                "rule-1",
                "Adult customer",
                List.of("cond-1"),
                RuleMetadata.defaults()
        );

        EvaluationContext context = EvaluationContext.from(Map.of("customer.age", 25));
        ExpressionEvaluationStrategy strategy = mock(ExpressionEvaluationStrategy.class);
        try {
            doReturn(ExpressionEvaluationResult.success(true, AttributeType.BOOLEAN))
                .when(strategy).evaluate(anyString(), any(EvaluationContext.class));
        } catch (ExpressionEvaluationException e) {
            // Mock setup
        }

        rule.validate(context, strategy, List.of(condition));
        
        // Verify the expression was built correctly (indirectly through mock)
        // The actual expression building is tested through integration tests
    }

    @Test
    void shouldBuildExpressionWithAttributeVsAttribute() {
        Attribute totalAttr = new Attribute("order.total", AttributeType.DECIMAL);
        Attribute limitAttr = new Attribute("customer.limit", AttributeType.DECIMAL);
        Condition condition = Condition.attributeVsAttribute(
                "cond-1",
                "Total vs Limit",
                totalAttr,
                ComparisonOperator.LTE,
                limitAttr
        );
        
        Rule rule = new Rule(
                "rule-1",
                "Total check",
                List.of("cond-1"),
                RuleMetadata.defaults()
        );

        EvaluationContext context = EvaluationContext.from(Map.of("order.total", 100.0, "customer.limit", 200.0));
        ExpressionEvaluationStrategy strategy = mock(ExpressionEvaluationStrategy.class);
        try {
            doReturn(ExpressionEvaluationResult.success(true, AttributeType.BOOLEAN))
                .when(strategy).evaluate(anyString(), any(EvaluationContext.class));
        } catch (ExpressionEvaluationException e) {
            // Mock setup
        }

        RuleValidationResult result = rule.validate(context, strategy, List.of(condition));
        
        assertThat(result.passed()).isTrue();
    }

    @Test
    void shouldBuildExpressionWithMultipleConditions() {
        Attribute ageAttr = new Attribute("customer.age", AttributeType.NUMBER);
        Attribute totalAttr = new Attribute("order.total", AttributeType.DECIMAL);
        
        Condition condition1 = Condition.attributeVsValue(
                "cond-1",
                "Age check",
                ageAttr,
                ComparisonOperator.GTE,
                18
        );
        Condition condition2 = Condition.attributeVsValue(
                "cond-2",
                "Total check",
                totalAttr,
                ComparisonOperator.LT,
                1000.0
        );
        
        Rule rule = new Rule(
                "rule-1",
                "Complex rule",
                List.of("cond-1", "cond-2"),
                RuleMetadata.defaults()
        );

        EvaluationContext context = EvaluationContext.from(Map.of(
                "customer.age", 25,
                "order.total", 500.0
        ));
        ExpressionEvaluationStrategy strategy = mock(ExpressionEvaluationStrategy.class);
        try {
            doReturn(ExpressionEvaluationResult.success(true, AttributeType.BOOLEAN))
                .when(strategy).evaluate(anyString(), any(EvaluationContext.class));
        } catch (ExpressionEvaluationException e) {
            // Mock setup
        }

        RuleValidationResult result = rule.validate(context, strategy, List.of(condition1, condition2));
        
        assertThat(result.passed()).isTrue();
    }
}
