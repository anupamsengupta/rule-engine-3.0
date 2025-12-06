package com.ruleengine.persistence.mapper;

import com.ruleengine.domain.attribute.Attribute;
import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.persistence.entity.AttributeEntity;
import com.ruleengine.persistence.entity.AttributeTypeEntity;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AttributeMapper.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
class AttributeMapperTest {

    @Test
    void shouldMapDomainToEntity() {
        Attribute domain = new Attribute("customer.age", AttributeType.NUMBER, "Customer age");

        AttributeEntity entity = AttributeMapper.toEntity(domain);

        assertThat(entity.getCode()).isEqualTo("customer.age");
        assertThat(entity.getType()).isEqualTo(AttributeTypeEntity.NUMBER);
        assertThat(entity.getDescription()).isEqualTo("Customer age");
    }

    @Test
    void shouldMapEntityToDomain() {
        AttributeEntity entity = new AttributeEntity("customer.age", "customer.age", AttributeTypeEntity.NUMBER, "Customer age", null);

        Attribute domain = AttributeMapper.toDomain(entity);

        assertThat(domain.code()).isEqualTo("customer.age");
        assertThat(domain.type()).isEqualTo(AttributeType.NUMBER);
        assertThat(domain.description()).contains("Customer age");
    }

    @Test
    void shouldHandleNullValues() {
        assertThat(AttributeMapper.toDomain(null)).isNull();
        assertThat(AttributeMapper.toEntity(null)).isNull();
    }

    @Test
    void shouldMapConstraints() {
        Map<String, Object> constraints = Map.of("min", 0, "max", 120);
        Attribute domain = new Attribute("customer.age", AttributeType.NUMBER, "Customer age", constraints);

        AttributeEntity entity = AttributeMapper.toEntity(domain);

        assertThat(entity.getConstraints()).isNotNull();
        assertThat(entity.getConstraints().get("min")).isEqualTo("0");
    }
}

