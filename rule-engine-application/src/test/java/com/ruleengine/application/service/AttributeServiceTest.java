package com.ruleengine.application.service;

import com.ruleengine.domain.attribute.Attribute;
import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.persistence.entity.AttributeEntity;
import com.ruleengine.persistence.entity.AttributeTypeEntity;
import com.ruleengine.persistence.repository.AttributeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AttributeService.
 *
 * Module: rule-engine-application
 * Layer: Application
 */
@ExtendWith(MockitoExtension.class)
class AttributeServiceTest {

    @Mock
    private AttributeRepository attributeRepository;

    @InjectMocks
    private AttributeService attributeService;

    private Attribute testAttribute;

    @BeforeEach
    void setUp() {
        testAttribute = new Attribute(
                "customer.age",
                "customer.age",
                AttributeType.NUMBER,
                Optional.of("Customer age in years"),
                Optional.of(Map.of("min", 0, "max", 150))
        );
    }

    @Test
    void shouldCreateAttribute() {
        // Given
        AttributeEntity entity = new AttributeEntity(
                "customer.age",
                "customer.age",
                AttributeTypeEntity.NUMBER,
                "Customer age in years",
                Map.of("min", "0", "max", "150")
        );
        when(attributeRepository.existsByCode("customer.age")).thenReturn(false);
        when(attributeRepository.save(any(AttributeEntity.class))).thenReturn(entity);

        // When
        Attribute created = attributeService.createAttribute(testAttribute);

        // Then
        assertThat(created).isNotNull();
        assertThat(created.code()).isEqualTo("customer.age");
        assertThat(created.type()).isEqualTo(AttributeType.NUMBER);
        verify(attributeRepository).save(any(AttributeEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingDuplicateAttribute() {
        // Given
        when(attributeRepository.existsByCode("customer.age")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> attributeService.createAttribute(testAttribute))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
        verify(attributeRepository, never()).save(any());
    }

    @Test
    void shouldGetAttributeByCode() {
        // Given
        AttributeEntity entity = new AttributeEntity(
                "customer.age",
                "customer.age",
                AttributeTypeEntity.NUMBER,
                "Customer age in years",
                null
        );
        when(attributeRepository.findByCode("customer.age")).thenReturn(Optional.of(entity));

        // When
        Optional<Attribute> result = attributeService.getAttributeByCode("customer.age");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().code()).isEqualTo("customer.age");
    }

    @Test
    void shouldReturnEmptyWhenAttributeNotFound() {
        // Given
        when(attributeRepository.findByCode("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<Attribute> result = attributeService.getAttributeByCode("nonexistent");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldUpdateAttribute() {
        // Given
        AttributeEntity existingEntity = new AttributeEntity(
                "customer.age",
                "customer.age",
                AttributeTypeEntity.NUMBER,
                "Old description",
                null
        );
        AttributeEntity savedEntity = new AttributeEntity(
                "customer.age",
                "customer.age",
                AttributeTypeEntity.NUMBER,
                "New description",
                Map.of("min", "0")
        );
        when(attributeRepository.findByCode("customer.age")).thenReturn(Optional.of(existingEntity));
        when(attributeRepository.save(any(AttributeEntity.class))).thenReturn(savedEntity);

        Attribute updated = new Attribute(
                "customer.age",
                "customer.age",
                AttributeType.STRING,
                Optional.of("New description"),
                Optional.of(Map.of("min", 0))
        );

        // When
        Attribute result = attributeService.updateAttribute(updated);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.description()).isPresent();
        assertThat(result.description().get()).isEqualTo("New description");
        verify(attributeRepository).save(any(AttributeEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentAttribute() {
        // Given
        when(attributeRepository.findByCode("nonexistent")).thenReturn(Optional.empty());
        Attribute nonExistent = new Attribute("nonexistent", AttributeType.STRING);

        // When/Then
        assertThatThrownBy(() -> attributeService.updateAttribute(nonExistent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldDeleteAttribute() {
        // Given
        when(attributeRepository.existsByCode("customer.age")).thenReturn(true);
        doNothing().when(attributeRepository).deleteById("customer.age");

        // When
        attributeService.deleteAttribute("customer.age");

        // Then
        verify(attributeRepository).deleteById("customer.age");
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentAttribute() {
        // Given
        when(attributeRepository.existsByCode("nonexistent")).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> attributeService.deleteAttribute("nonexistent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldCheckAttributeExists() {
        // Given
        when(attributeRepository.existsByCode("customer.age")).thenReturn(true);

        // When
        boolean exists = attributeService.attributeExists("customer.age");

        // Then
        assertThat(exists).isTrue();
    }
}

