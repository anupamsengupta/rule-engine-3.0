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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests for RuleSet CRUD operations.
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
class RuleSetCrudE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/rule-sets";
    }

    @BeforeEach
    void setUp() {
        // Create attributes and rules needed for rule sets
        createAttribute("customer.age", "NUMBER");
        createRule("rule-1", "Rule 1");
        createRule("rule-2", "Rule 2");
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

    private void createRule(String id, String name) {
        CreateRuleRequest ruleRequest = new CreateRuleRequest(
                id,
                name,
                List.of(new ConditionDto("customer.age", "NUMBER", "GT", 0)),
                1,
                true,
                null
        );
        restTemplate.postForEntity(
                "http://localhost:" + port + "/api/rules",
                ruleRequest,
                RuleDto.class
        );
    }

    @Test
    void shouldCreateRuleSet() {
        CreateRuleSetRequest request = new CreateRuleSetRequest(
                "ruleset-1",
                "Test rule set",
                List.of("rule-1", "rule-2"),
                false,
                "SPEL"
        );

        ResponseEntity<RuleSetDto> response = restTemplate.postForEntity(
                getBaseUrl(),
                request,
                RuleSetDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo("ruleset-1");
        assertThat(response.getBody().name()).isEqualTo("Test rule set");
        assertThat(response.getBody().ruleIds()).hasSize(2);
        assertThat(response.getBody().engineType()).isEqualTo("SPEL");
    }

    @Test
    void shouldGetRuleSetById() {
        // Create a rule set
        CreateRuleSetRequest createRequest = new CreateRuleSetRequest(
                "ruleset-get",
                "Test rule set",
                List.of("rule-1"),
                false,
                "MVEL"
        );
        restTemplate.postForEntity(getBaseUrl(), createRequest, RuleSetDto.class);

        // Get it
        ResponseEntity<RuleSetDto> response = restTemplate.getForEntity(
                getBaseUrl() + "/ruleset-get",
                RuleSetDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo("ruleset-get");
    }

    @Test
    void shouldGetAllRuleSets() {
        // Create multiple rule sets
        restTemplate.postForEntity(
                getBaseUrl(),
                new CreateRuleSetRequest("ruleset-all-1", "RuleSet 1", 
                        List.of("rule-1"), false, "SPEL"),
                RuleSetDto.class
        );
        restTemplate.postForEntity(
                getBaseUrl(),
                new CreateRuleSetRequest("ruleset-all-2", "RuleSet 2",
                        List.of("rule-2"), true, "JEXL"),
                RuleSetDto.class
        );

        ResponseEntity<RuleSetDto[]> response = restTemplate.getForEntity(
                getBaseUrl(),
                RuleSetDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldUpdateRuleSet() {
        // Create a rule set
        CreateRuleSetRequest createRequest = new CreateRuleSetRequest(
                "ruleset-update",
                "Original name",
                List.of("rule-1"),
                false,
                "SPEL"
        );
        restTemplate.postForEntity(getBaseUrl(), createRequest, RuleSetDto.class);

        // Update it
        UpdateRuleSetRequest updateRequest = new UpdateRuleSetRequest(
                "Updated name",
                List.of("rule-2"),
                true,
                "MVEL"
        );

        ResponseEntity<RuleSetDto> response = restTemplate.exchange(
                getBaseUrl() + "/ruleset-update",
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                RuleSetDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Updated name");
        assertThat(response.getBody().stopOnFirstFailure()).isTrue();
        assertThat(response.getBody().engineType()).isEqualTo("MVEL");
    }

    @Test
    void shouldDeleteRuleSet() {
        // Create a rule set
        CreateRuleSetRequest createRequest = new CreateRuleSetRequest(
                "ruleset-delete",
                "To be deleted",
                List.of("rule-1"),
                false,
                "SPEL"
        );
        restTemplate.postForEntity(getBaseUrl(), createRequest, RuleSetDto.class);

        // Delete it
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                getBaseUrl() + "/ruleset-delete",
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify it's deleted
        ResponseEntity<RuleSetDto> getResponse = restTemplate.getForEntity(
                getBaseUrl() + "/ruleset-delete",
                RuleSetDto.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnNotFoundForNonExistentRuleSet() {
        ResponseEntity<RuleSetDto> response = restTemplate.getForEntity(
                getBaseUrl() + "/nonexistent",
                RuleSetDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}

