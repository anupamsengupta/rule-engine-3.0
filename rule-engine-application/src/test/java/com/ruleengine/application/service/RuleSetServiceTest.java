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
        Rule rule = new Rule(
                "rule-1",
                "Test rule",
                List.of("cond-1"),
                null
        );
        testRuleSet = new RuleSet(
                "ruleset-1",
                "Test rule set",
                List.of(rule),
                false,
                EngineType.SPEL,
                "Validation"
        );
    }

    @Test
    void shouldCreateRuleSet() {
        // Given
        RuleSetEntity entity = new RuleSetEntity(
                "ruleset-1",
                "Test rule set",
                false,
                com.ruleengine.persistence.entity.EngineTypeEntity.SPEL,
                "Validation"
        );
        // Set up a rule with conditions for the rule set
        RuleEntity ruleEntity = new RuleEntity(
                "rule-1",
                "Test rule",
                1,
                true,
                null
        );
        ruleEntity.setConditionIds(new java.util.ArrayList<>(List.of("cond-1")));
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
                com.ruleengine.persistence.entity.EngineTypeEntity.SPEL,
                "Validation"
        );
        // Set up a rule with conditions for the rule set
        RuleEntity ruleEntity = new RuleEntity(
                "rule-1",
                "Test rule",
                1,
                true,
                null
        );
        ruleEntity.setConditionIds(new java.util.ArrayList<>(List.of("cond-1")));
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
                com.ruleengine.persistence.entity.EngineTypeEntity.SPEL,
                "Validation"
        );
        // Set up existing rule
        RuleEntity existingRuleEntity = new RuleEntity(
                "rule-1",
                "Test rule",
                1,
                true,
                null
        );
        existingRuleEntity.setConditionIds(new java.util.ArrayList<>(List.of("cond-1")));
        existingEntity.setRules(new java.util.ArrayList<>(List.of(existingRuleEntity)));
        
        RuleSetEntity savedEntity = new RuleSetEntity(
                "ruleset-1",
                "New name",
                true,
                com.ruleengine.persistence.entity.EngineTypeEntity.MVEL,
                "Validation"
        );
        savedEntity.setRules(new java.util.ArrayList<>(List.of(existingRuleEntity)));
        when(ruleSetRepository.findById("ruleset-1")).thenReturn(Optional.of(existingEntity));
        when(ruleSetRepository.save(any(RuleSetEntity.class))).thenReturn(savedEntity);

        RuleSet updated = new RuleSet(
                "ruleset-1",
                "New name",
                testRuleSet.rules(),
                true,
                EngineType.MVEL,
                "Validation"
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
                EngineType.SPEL,
                "Validation"
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

    @Test
    void shouldGetRuleSetsByCategory() {
        // Given
        RuleSetEntity entity1 = new RuleSetEntity(
                "ruleset-1",
                "Test rule set 1",
                false,
                com.ruleengine.persistence.entity.EngineTypeEntity.SPEL,
                "Pricing"
        );
        RuleEntity ruleEntity1 = new RuleEntity(
                "rule-1",
                "Test rule",
                1,
                true,
                null
        );
        ruleEntity1.setConditionIds(new java.util.ArrayList<>(List.of("cond-1")));
        entity1.setRules(new java.util.ArrayList<>(List.of(ruleEntity1)));
        
        RuleSetEntity entity2 = new RuleSetEntity(
                "ruleset-2",
                "Test rule set 2",
                false,
                com.ruleengine.persistence.entity.EngineTypeEntity.SPEL,
                "Pricing"
        );
        RuleEntity ruleEntity2 = new RuleEntity(
                "rule-2",
                "Test rule 2",
                1,
                true,
                null
        );
        ruleEntity2.setConditionIds(new java.util.ArrayList<>(List.of("cond-2")));
        entity2.setRules(new java.util.ArrayList<>(List.of(ruleEntity2)));
        
        when(ruleSetRepository.findByRuleCategory("Pricing")).thenReturn(List.of(entity1, entity2));

        // When
        List<RuleSet> result = ruleSetService.getRuleSetsByCategory("Pricing");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(rs -> "Pricing".equals(rs.ruleCategory()));
    }
}

