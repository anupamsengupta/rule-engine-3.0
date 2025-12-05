package com.ruleengine.domain.attribute;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Attribute domain model.
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
class AttributeTest {

    @Test
    void shouldCreateAttributeWithRequiredFields() {
        Attribute attribute = new Attribute("customer.age", AttributeType.NUMBER);

        assertThat(attribute.code()).isEqualTo("customer.age");
        assertThat(attribute.type()).isEqualTo(AttributeType.NUMBER);
        assertThat(attribute.description()).isEmpty();
        assertThat(attribute.constraints()).isEmpty();
    }

    @Test
    void shouldCreateAttributeWithDescription() {
        Attribute attribute = new Attribute("customer.age", AttributeType.NUMBER, "Customer age");

        assertThat(attribute.code()).isEqualTo("customer.age");
        assertThat(attribute.description()).contains("Customer age");
    }

    @Test
    void shouldThrowExceptionWhenCodeIsNull() {
        assertThatThrownBy(() -> new Attribute(null, AttributeType.NUMBER))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Attribute code cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenCodeIsBlank() {
        assertThatThrownBy(() -> new Attribute("  ", AttributeType.NUMBER))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Attribute code cannot be null or blank");
    }

    @Test
    void shouldThrowExceptionWhenTypeIsNull() {
        assertThatThrownBy(() -> new Attribute("customer.age", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Attribute type cannot be null");
    }
}

