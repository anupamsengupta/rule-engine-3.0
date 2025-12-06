package com.ruleengine.persistence.repository;

import com.ruleengine.persistence.entity.AttributeEntity;
import com.ruleengine.persistence.entity.AttributeTypeEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for AttributeRepository using Testcontainers.
 * <p>
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AttributeRepositoryIntegrationTest {

    @Autowired
    private AttributeRepository repository;

    @Test
    void shouldSaveAndRetrieveAttribute() {
        AttributeEntity entity = new AttributeEntity(
                "customer.age",
                "customer.age",
                AttributeTypeEntity.NUMBER,
                "Customer age",
                null
        );

        repository.save(entity);

        Optional<AttributeEntity> found = repository.findByCode("customer.age");
        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("customer.age");
        assertThat(found.get().getType()).isEqualTo(AttributeTypeEntity.NUMBER);
    }

    @Test
    void shouldCheckExistence() {
        AttributeEntity entity = new AttributeEntity(
                "customer.age",
                "customer.age",
                AttributeTypeEntity.NUMBER,
                null,
                null
        );

        repository.save(entity);

        assertThat(repository.existsByCode("customer.age")).isTrue();
        assertThat(repository.existsByCode("nonexistent")).isFalse();
    }
}

