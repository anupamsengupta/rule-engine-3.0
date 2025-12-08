package com.ruleengine.app.integration;

import com.ruleengine.api.dto.*;
import org.junit.jupiter.api.BeforeEach;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests for Condition CRUD operations.
 *
 * Module: rule-engine-app
 * Layer: App (E2E Tests)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb-ConditionCrudE2ETest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
class ConditionCrudE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/conditions";
    }

    @BeforeEach
    void setUp() {
        // Create attributes needed for conditions
        createAttribute("customer.age", "NUMBER");
        createAttribute("order.total", "DECIMAL");
        createAttribute("customer.limit", "DECIMAL");
    }

    private void createAttribute(String code, String type) {
        CreateAttributeRequest attrRequest = new CreateAttributeRequest(
                code, code, type, null, null
        );
        restTemplate.postForEntity(
                "http://localhost:" + port + "/api/attributes",
                attrRequest,
                AttributeDto.class
        );
    }

    @Test
    void shouldCreateCondition_AttributeVsValue() {
        CreateConditionRequest request = new CreateConditionRequest(
                "cond-1",
                "Age check",
                "customer.age",
                "GTE",
                null,
                18
        );

        ResponseEntity<ConditionDto> response = restTemplate.postForEntity(
                getBaseUrl(),
                request,
                ConditionDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo("cond-1");
        assertThat(response.getBody().name()).isEqualTo("Age check");
        assertThat(response.getBody().leftAttributeCode()).isEqualTo("customer.age");
        assertThat(response.getBody().operator()).isEqualTo("GTE");
        assertThat(response.getBody().rightAttributeCode()).isNull();
        assertThat(response.getBody().targetValue()).isEqualTo(18);
    }

    @Test
    void shouldCreateCondition_AttributeVsAttribute() {
        CreateConditionRequest request = new CreateConditionRequest(
                "cond-2",
                "Total vs Limit",
                "order.total",
                "LTE",
                "customer.limit",
                null
        );

        ResponseEntity<ConditionDto> response = restTemplate.postForEntity(
                getBaseUrl(),
                request,
                ConditionDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo("cond-2");
        assertThat(response.getBody().name()).isEqualTo("Total vs Limit");
        assertThat(response.getBody().leftAttributeCode()).isEqualTo("order.total");
        assertThat(response.getBody().operator()).isEqualTo("LTE");
        assertThat(response.getBody().rightAttributeCode()).isEqualTo("customer.limit");
        assertThat(response.getBody().targetValue()).isNull();
    }

    @Test
    void shouldGetConditionById() {
        // Create a condition
        CreateConditionRequest createRequest = new CreateConditionRequest(
                "cond-get",
                "Age check",
                "customer.age",
                "GT",
                null,
                0
        );
        restTemplate.postForEntity(getBaseUrl(), createRequest, ConditionDto.class);

        // Get it
        ResponseEntity<ConditionDto> response = restTemplate.getForEntity(
                getBaseUrl() + "/cond-get",
                ConditionDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo("cond-get");
    }

    @Test
    void shouldGetAllConditions() {
        // Create multiple conditions
        restTemplate.postForEntity(
                getBaseUrl(),
                new CreateConditionRequest("cond-all-1", "Age check", "customer.age", "GT", null, 0),
                ConditionDto.class
        );
        restTemplate.postForEntity(
                getBaseUrl(),
                new CreateConditionRequest("cond-all-2", "Total check", "order.total", "GT", null, 0),
                ConditionDto.class
        );

        ResponseEntity<ConditionDto[]> response = restTemplate.getForEntity(
                getBaseUrl(),
                ConditionDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldUpdateCondition() {
        // Create a condition
        CreateConditionRequest createRequest = new CreateConditionRequest(
                "cond-update",
                "Original name",
                "customer.age",
                "GT",
                null,
                0
        );
        restTemplate.postForEntity(getBaseUrl(), createRequest, ConditionDto.class);

        // Update it
        UpdateConditionRequest updateRequest = new UpdateConditionRequest(
                "Updated name",
                "customer.age",
                "GTE",
                null,
                18
        );
        ResponseEntity<ConditionDto> response = restTemplate.exchange(
                getBaseUrl() + "/cond-update",
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                ConditionDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Updated name");
        assertThat(response.getBody().operator()).isEqualTo("GTE");
        assertThat(response.getBody().targetValue()).isEqualTo(18);
    }

    @Test
    void shouldDeleteCondition() {
        // Create a condition
        CreateConditionRequest createRequest = new CreateConditionRequest(
                "cond-delete",
                "To be deleted",
                "customer.age",
                "GT",
                null,
                0
        );
        restTemplate.postForEntity(getBaseUrl(), createRequest, ConditionDto.class);

        // Delete it
        ResponseEntity<Void> response = restTemplate.exchange(
                getBaseUrl() + "/cond-delete",
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify it's deleted
        ResponseEntity<ConditionDto> getResponse = restTemplate.getForEntity(
                getBaseUrl() + "/cond-delete",
                ConditionDto.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnNotFoundForNonExistentCondition() {
        ResponseEntity<ConditionDto> response = restTemplate.getForEntity(
                getBaseUrl() + "/nonexistent",
                ConditionDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}

