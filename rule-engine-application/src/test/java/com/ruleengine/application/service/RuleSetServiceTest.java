package com.ruleengine.application.service;

import com.ruleengine.domain.factory.EngineType;
import com.ruleengine.domain.rule.Rule;
import com.ruleengine.domain.rule.RuleSet;
import com.ruleengine.persistence.entity.RuleEntity;
import com.ruleengine.persistence.entity.RuleSetEntity;
import com.ruleengine.persistence.repository.RuleSetRepository;
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
 * Unit tests for RuleSetService.
 *
 * Module: rule-engine-application
 * Layer: Application
 */
@ExtendWith(MockitoExtension.class)
class RuleSetServiceTest {

    @Mock
    private RuleSetRepository ruleSetRepository;

    @InjectMocks
    private RuleSetService ruleSetService;

    private RuleSet testRuleSet;

    @BeforeEach
    void setUp() {
        // Create a simple rule for the rule set
        com.ruleengine.domain.attribute.Attribute attribute = 
                new com.ruleengine.domain.attribute.Attribute("test.attr", com.ruleengine.domain.attribute.AttributeType.STRING);
        com.ruleengine.domain.rule.Condition condition = 
                new com.ruleengine.domain.rule.Condition(attribute, com.ruleengine.domain.operator.ComparisonOperator.EQ, "value");
        Rule rule = new Rule(
                "rule-1",
                "Test rule",
                List.of(condition),
                null
        );
        testRuleSet = new RuleSet(
                "ruleset-1",
                "Test rule set",
                List.of(rule),
                false,
                EngineType.SPEL
        );
    }

    @Test
    void shouldCreateRuleSet() {
        // Given
        RuleSetEntity entity = new RuleSetEntity(
                "ruleset-1",
                "Test rule set",
                false,
                com.ruleengine.persistence.entity.EngineTypeEntity.SPEL
        );
        // Set up a rule with conditions for the rule set
        RuleEntity ruleEntity = new RuleEntity(
                "rule-1",
                "Test rule",
                1,
                true,
                null
        );
        com.ruleengine.persistence.entity.AttributeEntity attrEntity = 
                new com.ruleengine.persistence.entity.AttributeEntity(
                        "test.attr",
                        "test.attr",
                        com.ruleengine.persistence.entity.AttributeTypeEntity.STRING,
                        null,
                        null
                );
        com.ruleengine.persistence.entity.ConditionEntity conditionEntity = 
                new com.ruleengine.persistence.entity.ConditionEntity(
                        ruleEntity,
                        attrEntity,
                        com.ruleengine.persistence.entity.ComparisonOperatorEntity.EQ,
                        "value",
                        "java.lang.String",
                        0
                );
        ruleEntity.setConditions(List.of(conditionEntity));
        entity.setRules(new java.util.ArrayList<>(List.of(ruleEntity)));
        when(ruleSetRepository.existsById("ruleset-1")).thenReturn(false);
        when(ruleSetRepository.save(any(RuleSetEntity.class))).thenReturn(entity);

        // When
        RuleSet created = ruleSetService.createRuleSet(testRuleSet);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.id()).isEqualTo("ruleset-1");
        assertThat(created.name()).isEqualTo("Test rule set");
        verify(ruleSetRepository).save(any(RuleSetEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateRuleSet() {
        // Given
        when(ruleSetRepository.existsById("ruleset-1")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> ruleSetService.createRuleSet(testRuleSet))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
        verify(ruleSetRepository, never()).save(any());
    }

    @Test
    void shouldGetRuleSetById() {
        // Given
        RuleSetEntity entity = new RuleSetEntity(
                "ruleset-1",
                "Test rule set",
                false,
                com.ruleengine.persistence.entity.EngineTypeEntity.SPEL
        );
        // Set up a rule with conditions for the rule set
        RuleEntity ruleEntity = new RuleEntity(
                "rule-1",
                "Test rule",
                1,
                true,
                null
        );
        com.ruleengine.persistence.entity.AttributeEntity attrEntity = 
                new com.ruleengine.persistence.entity.AttributeEntity(
                        "test.attr",
                        "test.attr",
                        com.ruleengine.persistence.entity.AttributeTypeEntity.STRING,
                        null,
                        null
                );
        com.ruleengine.persistence.entity.ConditionEntity conditionEntity = 
                new com.ruleengine.persistence.entity.ConditionEntity(
                        ruleEntity,
                        attrEntity,
                        com.ruleengine.persistence.entity.ComparisonOperatorEntity.EQ,
                        "value",
                        "java.lang.String",
                        0
                );
        ruleEntity.setConditions(List.of(conditionEntity));
        entity.setRules(new java.util.ArrayList<>(List.of(ruleEntity)));
        when(ruleSetRepository.findById("ruleset-1")).thenReturn(Optional.of(entity));

        // When
        Optional<RuleSet> result = ruleSetService.getRuleSetById("ruleset-1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo("ruleset-1");
    }

    @Test
    void shouldReturnEmptyWhenRuleSetNotFound() {
        // Given
        when(ruleSetRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<RuleSet> result = ruleSetService.getRuleSetById("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldUpdateRuleSet() {
        // Given
        RuleSetEntity existingEntity = new RuleSetEntity(
                "ruleset-1",
                "Old name",
                false,
                com.ruleengine.persistence.entity.EngineTypeEntity.SPEL
        );
        // Set up existing rule with conditions
        RuleEntity existingRuleEntity = new RuleEntity(
                "rule-1",
                "Test rule",
                1,
                true,
                null
        );
        com.ruleengine.persistence.entity.AttributeEntity attrEntity = 
                new com.ruleengine.persistence.entity.AttributeEntity(
                        "test.attr",
                        "test.attr",
                        com.ruleengine.persistence.entity.AttributeTypeEntity.STRING,
                        null,
                        null
                );
        com.ruleengine.persistence.entity.ConditionEntity conditionEntity = 
                new com.ruleengine.persistence.entity.ConditionEntity(
                        existingRuleEntity,
                        attrEntity,
                        com.ruleengine.persistence.entity.ComparisonOperatorEntity.EQ,
                        "value",
                        "java.lang.String",
                        0
                );
        existingRuleEntity.setConditions(new java.util.ArrayList<>(List.of(conditionEntity)));
        existingEntity.setRules(new java.util.ArrayList<>(List.of(existingRuleEntity)));
        
        RuleSetEntity savedEntity = new RuleSetEntity(
                "ruleset-1",
                "New name",
                true,
                com.ruleengine.persistence.entity.EngineTypeEntity.MVEL
        );
        savedEntity.setRules(new java.util.ArrayList<>(List.of(existingRuleEntity)));
        when(ruleSetRepository.findById("ruleset-1")).thenReturn(Optional.of(existingEntity));
        when(ruleSetRepository.save(any(RuleSetEntity.class))).thenReturn(savedEntity);

        RuleSet updated = new RuleSet(
                "ruleset-1",
                "New name",
                testRuleSet.rules(),
                true,
                EngineType.MVEL
        );

        // When
        RuleSet result = ruleSetService.updateRuleSet(updated);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("New name");
        assertThat(result.stopOnFirstFailure()).isTrue();
        verify(ruleSetRepository).save(any(RuleSetEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentRuleSet() {
        // Given
        when(ruleSetRepository.findById("nonexistent")).thenReturn(Optional.empty());
        RuleSet nonExistent = new RuleSet(
                "nonexistent",
                "Non-existent rule set",
                testRuleSet.rules(),
                false,
                EngineType.SPEL
        );

        // When/Then
        assertThatThrownBy(() -> ruleSetService.updateRuleSet(nonExistent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldDeleteRuleSet() {
        // Given
        when(ruleSetRepository.existsById("ruleset-1")).thenReturn(true);
        doNothing().when(ruleSetRepository).deleteById("ruleset-1");

        // When
        ruleSetService.deleteRuleSet("ruleset-1");

        // Then
        verify(ruleSetRepository).deleteById("ruleset-1");
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentRuleSet() {
        // Given
        when(ruleSetRepository.existsById("nonexistent")).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> ruleSetService.deleteRuleSet("nonexistent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldCheckRuleSetExists() {
        // Given
        when(ruleSetRepository.existsById("ruleset-1")).thenReturn(true);

        // When
        boolean exists = ruleSetService.ruleSetExists("ruleset-1");

        // Then
        assertThat(exists).isTrue();
    }
}

