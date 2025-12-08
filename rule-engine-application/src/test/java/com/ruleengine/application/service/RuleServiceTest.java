package com.ruleengine.application.service;

import com.ruleengine.domain.attribute.Attribute;
import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.domain.operator.ComparisonOperator;
import com.ruleengine.domain.rule.Condition;
import com.ruleengine.domain.rule.Rule;
import com.ruleengine.domain.rule.RuleMetadata;
import com.ruleengine.persistence.entity.RuleEntity;
import com.ruleengine.persistence.repository.RuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RuleService.
 *
 * Module: rule-engine-application
 * Layer: Application
 */
@ExtendWith(MockitoExtension.class)
class RuleServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @InjectMocks
    private RuleService ruleService;

    private Rule testRule;

    @BeforeEach
    void setUp() {
        testRule = new Rule(
                "rule-1",
                "Adult customer rule",
                List.of("cond-1"),
                new RuleMetadata(10, true, Set.of("customer", "age"))
        );
    }

    @Test
    void shouldCreateRule() {
        // Given
        RuleEntity entity = new RuleEntity(
                "rule-1",
                "Adult customer rule",
                10,
                true,
                Set.of("customer", "age")
        );
        entity.setConditionIds(new java.util.ArrayList<>(List.of("cond-1")));
        when(ruleRepository.existsById("rule-1")).thenReturn(false);
        when(ruleRepository.save(any(RuleEntity.class))).thenReturn(entity);

        // When
        Rule created = ruleService.createRule(testRule);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.id()).isEqualTo("rule-1");
        assertThat(created.name()).isEqualTo("Adult customer rule");
        assertThat(created.conditionIds()).containsExactly("cond-1");
        verify(ruleRepository).save(any(RuleEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateRule() {
        // Given
        when(ruleRepository.existsById("rule-1")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> ruleService.createRule(testRule))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
        verify(ruleRepository, never()).save(any());
    }

    @Test
    void shouldGetRuleById() {
        // Given
        RuleEntity entity = new RuleEntity(
                "rule-1",
                "Adult customer rule",
                10,
                true,
                Set.of("customer")
        );
        entity.setConditionIds(new java.util.ArrayList<>(List.of("cond-1")));
        when(ruleRepository.findById("rule-1")).thenReturn(Optional.of(entity));

        // When
        Optional<Rule> result = ruleService.getRuleById("rule-1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo("rule-1");
        assertThat(result.get().conditionIds()).containsExactly("cond-1");
    }

    @Test
    void shouldReturnEmptyWhenRuleNotFound() {
        // Given
        when(ruleRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<Rule> result = ruleService.getRuleById("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldUpdateRule() {
        // Given
        RuleEntity existingEntity = new RuleEntity(
                "rule-1",
                "Old name",
                5,
                false,
                Set.of()
        );
        existingEntity.setConditionIds(new java.util.ArrayList<>(List.of("cond-1")));
        
        RuleEntity savedEntity = new RuleEntity(
                "rule-1",
                "New name",
                20,
                true,
                Set.of("new-tag")
        );
        savedEntity.setConditionIds(new java.util.ArrayList<>(List.of("cond-2")));
        when(ruleRepository.findById("rule-1")).thenReturn(Optional.of(existingEntity));
        when(ruleRepository.save(any(RuleEntity.class))).thenReturn(savedEntity);

        Rule updated = new Rule(
                "rule-1",
                "New name",
                List.of("cond-2"),
                new RuleMetadata(20, true, Set.of("new-tag"))
        );

        // When
        Rule result = ruleService.updateRule(updated);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("New name");
        assertThat(result.conditionIds()).containsExactly("cond-2");
        verify(ruleRepository).save(any(RuleEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentRule() {
        // Given
        when(ruleRepository.findById("nonexistent")).thenReturn(Optional.empty());
        Rule nonExistent = new Rule(
                "nonexistent",
                "Non-existent rule",
                testRule.conditionIds(),
                testRule.metadata()
        );

        // When/Then
        assertThatThrownBy(() -> ruleService.updateRule(nonExistent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldDeleteRule() {
        // Given
        when(ruleRepository.existsById("rule-1")).thenReturn(true);
        doNothing().when(ruleRepository).deleteById("rule-1");

        // When
        ruleService.deleteRule("rule-1");

        // Then
        verify(ruleRepository).deleteById("rule-1");
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentRule() {
        // Given
        when(ruleRepository.existsById("nonexistent")).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> ruleService.deleteRule("nonexistent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldCheckRuleExists() {
        // Given
        when(ruleRepository.existsById("rule-1")).thenReturn(true);

        // When
        boolean exists = ruleService.ruleExists("rule-1");

        // Then
        assertThat(exists).isTrue();
    }
}

