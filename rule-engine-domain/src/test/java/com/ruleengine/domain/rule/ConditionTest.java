package com.ruleengine.domain.rule;

import com.ruleengine.domain.attribute.Attribute;
import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.domain.operator.ComparisonOperator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Condition domain model.
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
class ConditionTest {

    @Test
    void shouldCreateAttributeVsValueCondition() {
        Attribute ageAttr = new Attribute("customer.age", AttributeType.NUMBER);
        Condition condition = Condition.attributeVsValue(
                "cond-1",
                "Age check",
                ageAttr,
                ComparisonOperator.GTE,
                18
        );

        assertThat(condition.id()).isEqualTo("cond-1");
        assertThat(condition.name()).isEqualTo("Age check");
        assertThat(condition.leftAttribute()).isEqualTo(ageAttr);
        assertThat(condition.operator()).isEqualTo(ComparisonOperator.GTE);
        assertThat(condition.rightAttribute()).isEmpty();
        assertThat(condition.targetValue()).isPresent();
        assertThat(condition.targetValue().get()).isEqualTo(18);
    }

    @Test
    void shouldCreateAttributeVsAttributeCondition() {
        Attribute totalAttr = new Attribute("order.total", AttributeType.DECIMAL);
        Attribute limitAttr = new Attribute("customer.limit", AttributeType.DECIMAL);
        Condition condition = Condition.attributeVsAttribute(
                "cond-2",
                "Total vs Limit",
                totalAttr,
                ComparisonOperator.LTE,
                limitAttr
        );

        assertThat(condition.id()).isEqualTo("cond-2");
        assertThat(condition.name()).isEqualTo("Total vs Limit");
        assertThat(condition.leftAttribute()).isEqualTo(totalAttr);
        assertThat(condition.operator()).isEqualTo(ComparisonOperator.LTE);
        assertThat(condition.rightAttribute()).isPresent();
        assertThat(condition.rightAttribute().get()).isEqualTo(limitAttr);
        assertThat(condition.targetValue()).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        Attribute ageAttr = new Attribute("customer.age", AttributeType.NUMBER);
        
        assertThatThrownBy(() -> Condition.attributeVsValue(
                null,
                "Age check",
                ageAttr,
                ComparisonOperator.GTE,
                18
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Condition id cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenIdIsBlank() {
        Attribute ageAttr = new Attribute("customer.age", AttributeType.NUMBER);
        
        assertThatThrownBy(() -> Condition.attributeVsValue(
                "   ",
                "Age check",
                ageAttr,
                ComparisonOperator.GTE,
                18
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Condition id cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        Attribute ageAttr = new Attribute("customer.age", AttributeType.NUMBER);
        
        assertThatThrownBy(() -> Condition.attributeVsValue(
                "cond-1",
                null,
                ageAttr,
                ComparisonOperator.GTE,
                18
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Condition name cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        Attribute ageAttr = new Attribute("customer.age", AttributeType.NUMBER);
        
        assertThatThrownBy(() -> Condition.attributeVsValue(
                "cond-1",
                "   ",
                ageAttr,
                ComparisonOperator.GTE,
                18
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Condition name cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenLeftAttributeIsNull() {
        assertThatThrownBy(() -> Condition.attributeVsValue(
                "cond-1",
                "Age check",
                null,
                ComparisonOperator.GTE,
                18
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Condition leftAttribute cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenOperatorIsNull() {
        Attribute ageAttr = new Attribute("customer.age", AttributeType.NUMBER);
        
        assertThatThrownBy(() -> new Condition(
                "cond-1",
                "Age check",
                ageAttr,
                null,
                java.util.Optional.empty(),
                java.util.Optional.of(18)
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Condition operator cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenBothRightAttributeAndTargetValueAreMissing() {
        Attribute ageAttr = new Attribute("customer.age", AttributeType.NUMBER);
        
        assertThatThrownBy(() -> new Condition(
                "cond-1",
                "Age check",
                ageAttr,
                ComparisonOperator.GTE,
                java.util.Optional.empty(),
                java.util.Optional.empty()
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Condition must have either rightAttribute or targetValue");
    }

    @Test
    void shouldThrowExceptionWhenBothRightAttributeAndTargetValueArePresent() {
        Attribute ageAttr = new Attribute("customer.age", AttributeType.NUMBER);
        Attribute limitAttr = new Attribute("customer.limit", AttributeType.NUMBER);
        
        assertThatThrownBy(() -> new Condition(
                "cond-1",
                "Age check",
                ageAttr,
                ComparisonOperator.GTE,
                java.util.Optional.of(limitAttr),
                java.util.Optional.of(18)
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Condition cannot have both rightAttribute and targetValue");
    }
}

