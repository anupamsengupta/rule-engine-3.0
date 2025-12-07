package com.ruleengine.application.service;

import com.ruleengine.domain.expression.Expression;
import com.ruleengine.persistence.entity.ExpressionEntity;
import com.ruleengine.persistence.repository.ExpressionRepository;
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
 * Unit tests for ExpressionService.
 *
 * Module: rule-engine-application
 * Layer: Application
 */
@ExtendWith(MockitoExtension.class)
class ExpressionServiceTest {

    @Mock
    private ExpressionRepository expressionRepository;

    @InjectMocks
    private ExpressionService expressionService;

    private Expression testExpression;

    @BeforeEach
    void setUp() {
        testExpression = new Expression(
                Optional.of("expr-1"),
                "x > y && y > z"
        );
    }

    @Test
    void shouldCreateExpression() {
        // Given
        ExpressionEntity entity = new ExpressionEntity(
                "expr-1",
                "x > y && y > z",
                null
        );
        when(expressionRepository.existsById("expr-1")).thenReturn(false);
        when(expressionRepository.save(any(ExpressionEntity.class))).thenReturn(entity);

        // When
        Expression created = expressionService.createExpression(testExpression);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.id()).isPresent();
        assertThat(created.expressionString()).isEqualTo("x > y && y > z");
        verify(expressionRepository).save(any(ExpressionEntity.class));
    }

    @Test
    void shouldGenerateIdWhenCreatingExpressionWithoutId() {
        // Given
        Expression expressionWithoutId = new Expression("x > y");
        ExpressionEntity entity = new ExpressionEntity(
                "generated-id",
                "x > y",
                null
        );
        when(expressionRepository.existsById(anyString())).thenReturn(false);
        when(expressionRepository.save(any(ExpressionEntity.class))).thenReturn(entity);

        // When
        Expression created = expressionService.createExpression(expressionWithoutId);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.id()).isPresent();
        verify(expressionRepository).save(any(ExpressionEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateExpression() {
        // Given
        when(expressionRepository.existsById("expr-1")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> expressionService.createExpression(testExpression))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
        verify(expressionRepository, never()).save(any());
    }

    @Test
    void shouldGetExpressionById() {
        // Given
        ExpressionEntity entity = new ExpressionEntity(
                "expr-1",
                "x > y && y > z",
                null
        );
        when(expressionRepository.findById("expr-1")).thenReturn(Optional.of(entity));

        // When
        Optional<Expression> result = expressionService.getExpressionById("expr-1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().id()).isPresent();
        assertThat(result.get().id().get()).isEqualTo("expr-1");
    }

    @Test
    void shouldReturnEmptyWhenExpressionNotFound() {
        // Given
        when(expressionRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<Expression> result = expressionService.getExpressionById("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldUpdateExpression() {
        // Given
        ExpressionEntity existingEntity = new ExpressionEntity(
                "expr-1",
                "old expression",
                null
        );
        ExpressionEntity savedEntity = new ExpressionEntity(
                "expr-1",
                "new expression",
                null
        );
        when(expressionRepository.findById("expr-1")).thenReturn(Optional.of(existingEntity));
        when(expressionRepository.save(any(ExpressionEntity.class))).thenReturn(savedEntity);

        Expression updated = new Expression(
                Optional.of("expr-1"),
                "new expression"
        );

        // When
        Expression result = expressionService.updateExpression(updated);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.expressionString()).isEqualTo("new expression");
        verify(expressionRepository).save(any(ExpressionEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingExpressionWithoutId() {
        // Given
        Expression expressionWithoutId = new Expression("x > y");

        // When/Then
        assertThatThrownBy(() -> expressionService.updateExpression(expressionWithoutId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required for update");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentExpression() {
        // Given
        when(expressionRepository.findById("nonexistent")).thenReturn(Optional.empty());
        Expression nonExistent = new Expression(Optional.of("nonexistent"), "x > y");

        // When/Then
        assertThatThrownBy(() -> expressionService.updateExpression(nonExistent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldDeleteExpression() {
        // Given
        when(expressionRepository.existsById("expr-1")).thenReturn(true);
        doNothing().when(expressionRepository).deleteById("expr-1");

        // When
        expressionService.deleteExpression("expr-1");

        // Then
        verify(expressionRepository).deleteById("expr-1");
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentExpression() {
        // Given
        when(expressionRepository.existsById("nonexistent")).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> expressionService.deleteExpression("nonexistent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldCheckExpressionExists() {
        // Given
        when(expressionRepository.existsById("expr-1")).thenReturn(true);

        // When
        boolean exists = expressionService.expressionExists("expr-1");

        // Then
        assertThat(exists).isTrue();
    }
}

