package com.ruleengine.application.service;

import com.ruleengine.domain.attribute.Attribute;
import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.domain.operator.ComparisonOperator;
import com.ruleengine.domain.rule.Condition;
import com.ruleengine.persistence.entity.ConditionEntity;
import com.ruleengine.persistence.repository.ConditionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConditionService.
 *
 * Module: rule-engine-application
 * Layer: Application
 */
@ExtendWith(MockitoExtension.class)
class ConditionServiceTest {

    @Mock
    private ConditionRepository conditionRepository;

    @InjectMocks
    private ConditionService conditionService;

    private Condition testCondition;

    @BeforeEach
    void setUp() {
        Attribute attribute = new Attribute("customer.age", AttributeType.NUMBER);
        testCondition = Condition.attributeVsValue(
                "cond-1",
                "Age check",
                attribute,
                ComparisonOperator.GTE,
                18
        );
    }

    @Test
    void shouldCreateCondition() {
        // Given
        ConditionEntity entity = new ConditionEntity(
                "cond-1",
                "Age check",
                com.ruleengine.persistence.mapper.AttributeMapper.toEntity(testCondition.leftAttribute()),
                com.ruleengine.persistence.entity.ComparisonOperatorEntity.GTE,
                null,
                "18",
                "java.lang.Integer"
        );
        when(conditionRepository.existsById("cond-1")).thenReturn(false);
        when(conditionRepository.save(any(ConditionEntity.class))).thenReturn(entity);

        // When
        Condition created = conditionService.createCondition(testCondition);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.id()).isEqualTo("cond-1");
        assertThat(created.name()).isEqualTo("Age check");
        verify(conditionRepository).save(any(ConditionEntity.class));
    }

    @Test
    void shouldCreateAttributeVsAttributeCondition() {
        // Given
        Attribute leftAttr = new Attribute("order.total", AttributeType.DECIMAL);
        Attribute rightAttr = new Attribute("customer.limit", AttributeType.DECIMAL);
        Condition condition = Condition.attributeVsAttribute(
                "cond-2",
                "Total vs Limit",
                leftAttr,
                ComparisonOperator.LTE,
                rightAttr
        );
        
        ConditionEntity entity = new ConditionEntity(
                "cond-2",
                "Total vs Limit",
                com.ruleengine.persistence.mapper.AttributeMapper.toEntity(leftAttr),
                com.ruleengine.persistence.entity.ComparisonOperatorEntity.LTE,
                com.ruleengine.persistence.mapper.AttributeMapper.toEntity(rightAttr),
                null,
                null
        );
        when(conditionRepository.existsById("cond-2")).thenReturn(false);
        when(conditionRepository.save(any(ConditionEntity.class))).thenReturn(entity);

        // When
        Condition created = conditionService.createCondition(condition);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.id()).isEqualTo("cond-2");
        assertThat(created.rightAttribute()).isPresent();
        verify(conditionRepository).save(any(ConditionEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateCondition() {
        // Given
        when(conditionRepository.existsById("cond-1")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> conditionService.createCondition(testCondition))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
        verify(conditionRepository, never()).save(any());
    }

    @Test
    void shouldGetConditionById() {
        // Given
        ConditionEntity entity = new ConditionEntity(
                "cond-1",
                "Age check",
                com.ruleengine.persistence.mapper.AttributeMapper.toEntity(testCondition.leftAttribute()),
                com.ruleengine.persistence.entity.ComparisonOperatorEntity.GTE,
                null,
                "18",
                "java.lang.Integer"
        );
        when(conditionRepository.findById("cond-1")).thenReturn(Optional.of(entity));

        // When
        Optional<Condition> result = conditionService.getConditionById("cond-1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo("cond-1");
    }

    @Test
    void shouldReturnEmptyWhenConditionNotFound() {
        // Given
        when(conditionRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<Condition> result = conditionService.getConditionById("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldUpdateCondition() {
        // Given
        ConditionEntity existingEntity = new ConditionEntity(
                "cond-1",
                "Old name",
                com.ruleengine.persistence.mapper.AttributeMapper.toEntity(testCondition.leftAttribute()),
                com.ruleengine.persistence.entity.ComparisonOperatorEntity.GTE,
                null,
                "18",
                "java.lang.Integer"
        );
        ConditionEntity savedEntity = new ConditionEntity(
                "cond-1",
                "New name",
                com.ruleengine.persistence.mapper.AttributeMapper.toEntity(testCondition.leftAttribute()),
                com.ruleengine.persistence.entity.ComparisonOperatorEntity.GT,
                null,
                "21",
                "java.lang.Integer"
        );
        when(conditionRepository.findById("cond-1")).thenReturn(Optional.of(existingEntity));
        when(conditionRepository.save(any(ConditionEntity.class))).thenReturn(savedEntity);

        Attribute attribute = new Attribute("customer.age", AttributeType.NUMBER);
        Condition updated = Condition.attributeVsValue(
                "cond-1",
                "New name",
                attribute,
                ComparisonOperator.GT,
                21
        );

        // When
        Condition result = conditionService.updateCondition(updated);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("New name");
        verify(conditionRepository).save(any(ConditionEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentCondition() {
        // Given
        when(conditionRepository.findById("nonexistent")).thenReturn(Optional.empty());
        Attribute attribute = new Attribute("customer.age", AttributeType.NUMBER);
        Condition nonExistent = Condition.attributeVsValue(
                "nonexistent",
                "Non-existent",
                attribute,
                ComparisonOperator.GTE,
                18
        );

        // When/Then
        assertThatThrownBy(() -> conditionService.updateCondition(nonExistent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldDeleteCondition() {
        // Given
        when(conditionRepository.existsById("cond-1")).thenReturn(true);
        doNothing().when(conditionRepository).deleteById("cond-1");

        // When
        conditionService.deleteCondition("cond-1");

        // Then
        verify(conditionRepository).deleteById("cond-1");
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentCondition() {
        // Given
        when(conditionRepository.existsById("nonexistent")).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> conditionService.deleteCondition("nonexistent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldCheckConditionExists() {
        // Given
        when(conditionRepository.existsById("cond-1")).thenReturn(true);

        // When
        boolean exists = conditionService.conditionExists("cond-1");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldGetConditionsByIds() {
        // Given
        Attribute attr1 = new Attribute("customer.age", AttributeType.NUMBER);
        Attribute attr2 = new Attribute("order.total", AttributeType.DECIMAL);
        
        ConditionEntity entity1 = new ConditionEntity(
                "cond-1",
                "Age check",
                com.ruleengine.persistence.mapper.AttributeMapper.toEntity(attr1),
                com.ruleengine.persistence.entity.ComparisonOperatorEntity.GTE,
                null,
                "18",
                "java.lang.Integer"
        );
        ConditionEntity entity2 = new ConditionEntity(
                "cond-2",
                "Total check",
                com.ruleengine.persistence.mapper.AttributeMapper.toEntity(attr2),
                com.ruleengine.persistence.entity.ComparisonOperatorEntity.LT,
                null,
                "1000.0",
                "java.lang.Double"
        );
        when(conditionRepository.findById("cond-1")).thenReturn(Optional.of(entity1));
        when(conditionRepository.findById("cond-2")).thenReturn(Optional.of(entity2));

        // When
        List<Condition> conditions = conditionService.getConditionsByIds(List.of("cond-1", "cond-2"));

        // Then
        assertThat(conditions).hasSize(2);
        assertThat(conditions.get(0).id()).isEqualTo("cond-1");
        assertThat(conditions.get(1).id()).isEqualTo("cond-2");
    }

    @Test
    void shouldThrowExceptionWhenConditionIdNotFoundInGetConditionsByIds() {
        // Given
        when(conditionRepository.findById("cond-1")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> conditionService.getConditionsByIds(List.of("cond-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}

