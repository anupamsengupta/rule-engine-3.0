package com.ruleengine.app.integration;

import com.ruleengine.api.dto.*;
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
 * End-to-end tests for Expression CRUD operations.
 *
 * Module: rule-engine-app
 * Layer: App (E2E Tests)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class ExpressionCrudE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/expressions";
    }

    @Test
    void shouldCreateExpression() {
        CreateExpressionRequest request = new CreateExpressionRequest(
                "expr-1",
                "x > y && y > z",
                "Complex expression"
        );

        ResponseEntity<ExpressionDto> response = restTemplate.postForEntity(
                getBaseUrl(),
                request,
                ExpressionDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo("expr-1");
        assertThat(response.getBody().expressionString()).isEqualTo("x > y && y > z");
    }

    @Test
    void shouldCreateExpressionWithoutId() {
        CreateExpressionRequest request = new CreateExpressionRequest(
                null,
                "a + b",
                null
        );

        ResponseEntity<ExpressionDto> response = restTemplate.postForEntity(
                getBaseUrl(),
                request,
                ExpressionDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull(); // ID should be auto-generated
        assertThat(response.getBody().expressionString()).isEqualTo("a + b");
    }

    @Test
    void shouldGetExpressionById() {
        // Create an expression
        CreateExpressionRequest createRequest = new CreateExpressionRequest(
                "expr-get",
                "x * 2",
                "Double x"
        );
        restTemplate.postForEntity(getBaseUrl(), createRequest, ExpressionDto.class);

        // Get it
        ResponseEntity<ExpressionDto> response = restTemplate.getForEntity(
                getBaseUrl() + "/expr-get",
                ExpressionDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo("expr-get");
    }

    @Test
    void shouldGetAllExpressions() {
        // Create multiple expressions
        restTemplate.postForEntity(
                getBaseUrl(),
                new CreateExpressionRequest("expr-all-1", "x > 0", null),
                ExpressionDto.class
        );
        restTemplate.postForEntity(
                getBaseUrl(),
                new CreateExpressionRequest("expr-all-2", "y < 100", null),
                ExpressionDto.class
        );

        ResponseEntity<ExpressionDto[]> response = restTemplate.getForEntity(
                getBaseUrl(),
                ExpressionDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldUpdateExpression() {
        // Create an expression
        CreateExpressionRequest createRequest = new CreateExpressionRequest(
                "expr-update",
                "old expression",
                "Original description"
        );
        restTemplate.postForEntity(getBaseUrl(), createRequest, ExpressionDto.class);

        // Update it
        UpdateExpressionRequest updateRequest = new UpdateExpressionRequest(
                "new expression",
                "Updated description"
        );

        ResponseEntity<ExpressionDto> response = restTemplate.exchange(
                getBaseUrl() + "/expr-update",
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                ExpressionDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().expressionString()).isEqualTo("new expression");
    }

    @Test
    void shouldDeleteExpression() {
        // Create an expression
        CreateExpressionRequest createRequest = new CreateExpressionRequest(
                "expr-delete",
                "to be deleted",
                null
        );
        restTemplate.postForEntity(getBaseUrl(), createRequest, ExpressionDto.class);

        // Delete it
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                getBaseUrl() + "/expr-delete",
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify it's deleted
        ResponseEntity<ExpressionDto> getResponse = restTemplate.getForEntity(
                getBaseUrl() + "/expr-delete",
                ExpressionDto.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnNotFoundForNonExistentExpression() {
        ResponseEntity<ExpressionDto> response = restTemplate.getForEntity(
                getBaseUrl() + "/nonexistent",
                ExpressionDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldEvaluateExpressionAfterCreation() {
        // Create an expression
        CreateExpressionRequest createRequest = new CreateExpressionRequest(
                "expr-eval",
                "x + y",
                null
        );
        restTemplate.postForEntity(getBaseUrl(), createRequest, ExpressionDto.class);

        // Evaluate it
        ExpressionEvaluationRequest evalRequest = new ExpressionEvaluationRequest(
                "expr-eval",
                "x + y",
                Map.of("x", 10, "y", 20)
        );

        ResponseEntity<ExpressionEvaluationResponse> response = restTemplate.postForEntity(
                getBaseUrl() + "/evaluate",
                evalRequest,
                ExpressionEvaluationResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().value()).isEqualTo(30);
    }
}

