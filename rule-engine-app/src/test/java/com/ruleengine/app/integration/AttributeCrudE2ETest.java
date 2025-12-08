package com.ruleengine.app.integration;

import com.ruleengine.api.dto.AttributeDto;
import com.ruleengine.api.dto.CreateAttributeRequest;
import com.ruleengine.api.dto.UpdateAttributeRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests for Attribute CRUD operations.
 *
 * Module: rule-engine-app
 * Layer: App (E2E Tests)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb-AttributeCrudE2ETest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
class AttributeCrudE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/attributes";
    }

    @Test
    void shouldCreateAttribute() {
        CreateAttributeRequest request = new CreateAttributeRequest(
                "customer.age",
                "customer.age",
                "NUMBER",
                "Customer age in years",
                Map.of("min", 0, "max", 150)
        );

        ResponseEntity<AttributeDto> response = restTemplate.postForEntity(
                getBaseUrl(),
                request,
                AttributeDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("customer.age");
        assertThat(response.getBody().type()).isEqualTo("NUMBER");
    }

    @Test
    void shouldGetAttributeByCode() {
        // First create an attribute
        CreateAttributeRequest createRequest = new CreateAttributeRequest(
                "order.total",
                "order.total",
                "DECIMAL",
                "Order total amount",
                null
        );
        restTemplate.postForEntity(getBaseUrl(), createRequest, AttributeDto.class);

        // Then retrieve it
        ResponseEntity<AttributeDto> response = restTemplate.getForEntity(
                getBaseUrl() + "/order.total",
                AttributeDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("order.total");
    }

    @Test
    void shouldGetAllAttributes() {
        // Create a couple of attributes
        restTemplate.postForEntity(
                getBaseUrl(),
                new CreateAttributeRequest("attr1", "attr1", "STRING", "Attribute 1", null),
                AttributeDto.class
        );
        restTemplate.postForEntity(
                getBaseUrl(),
                new CreateAttributeRequest("attr2", "attr2", "NUMBER", "Attribute 2", null),
                AttributeDto.class
        );

        ResponseEntity<AttributeDto[]> response = restTemplate.getForEntity(
                getBaseUrl(),
                AttributeDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldUpdateAttribute() {
        // Create an attribute
        CreateAttributeRequest createRequest = new CreateAttributeRequest(
                "updatable.attr",
                "updatable.attr",
                "STRING",
                "Original description",
                null
        );
        restTemplate.postForEntity(getBaseUrl(), createRequest, AttributeDto.class);

        // Update it
        UpdateAttributeRequest updateRequest = new UpdateAttributeRequest(
                "updatable.attr",
                "STRING",
                "Updated description",
                Map.of("maxLength", 100)
        );

        ResponseEntity<AttributeDto> response = restTemplate.exchange(
                getBaseUrl() + "/updatable.attr",
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                AttributeDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().description()).isEqualTo("Updated description");
    }

    @Test
    void shouldDeleteAttribute() {
        // Create an attribute
        CreateAttributeRequest createRequest = new CreateAttributeRequest(
                "deletable.attr",
                "deletable.attr",
                "BOOLEAN",
                "To be deleted",
                null
        );
        restTemplate.postForEntity(getBaseUrl(), createRequest, AttributeDto.class);

        // Delete it
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                getBaseUrl() + "/deletable.attr",
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify it's deleted
        ResponseEntity<AttributeDto> getResponse = restTemplate.getForEntity(
                getBaseUrl() + "/deletable.attr",
                AttributeDto.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnNotFoundForNonExistentAttribute() {
        ResponseEntity<AttributeDto> response = restTemplate.getForEntity(
                getBaseUrl() + "/nonexistent",
                AttributeDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}

