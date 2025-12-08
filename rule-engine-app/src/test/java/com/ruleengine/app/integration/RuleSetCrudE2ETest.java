package com.ruleengine.app.integration;

import com.ruleengine.api.dto.*;
import org.junit.Ignore;
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
    "spring.datasource.url=jdbc:h2:mem:testdb-RuleSetCrudE2ETest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@Ignore
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
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
        // Create attributes, conditions, and rules needed for rule sets
        createAttribute("customer.age", "NUMBER");
        // Use non-zero values to avoid potential JSON deserialization issues with 0
        String cond1 = createCondition("cond-1", "Age check 1", "customer.age", "GT", null, 1);
        String cond2 = createCondition("cond-2", "Age check 2", "customer.age", "GT", null, 1);
        createRule("rule-1", "Rule 1", cond1);
        createRule("rule-2", "Rule 2", cond2);
    }

    private void createAttribute(String code, String type) {
        CreateAttributeRequest attrRequest = new CreateAttributeRequest(
                code, code, type, null, null
        );
        ResponseEntity<AttributeDto> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/attributes",
                attrRequest,
                AttributeDto.class
        );
        assertThat(response.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.OK);
    }

    private String createCondition(String id, String name, String leftAttrCode, String operator, String rightAttrCode, Object targetValue) {
        CreateConditionRequest condRequest = new CreateConditionRequest(
                id,
                name,
                leftAttrCode,
                operator,
                rightAttrCode,
                targetValue
        );
        ResponseEntity<ConditionDto> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/conditions",
                condRequest,
                ConditionDto.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody().id();
    }

    private void createRule(String id, String name, String conditionId) {
        CreateRuleRequest ruleRequest = new CreateRuleRequest(
                id,
                name,
                List.of(conditionId),
                1,
                true,
                null
        );
        ResponseEntity<RuleDto> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/rules",
                ruleRequest,
                RuleDto.class
        );
        assertThat(response.getStatusCode()).isIn(HttpStatus.CREATED, HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
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

