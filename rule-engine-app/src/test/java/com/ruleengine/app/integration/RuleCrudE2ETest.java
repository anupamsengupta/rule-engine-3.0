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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests for Rule CRUD operations.
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
class RuleCrudE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/rules";
    }

    @BeforeEach
    void setUp() {
        // Create attributes needed for rules
        createAttribute("customer.age", "NUMBER");
        createAttribute("order.total", "DECIMAL");
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
    void shouldCreateRule() {
        CreateRuleRequest request = new CreateRuleRequest(
                "rule-1",
                "Adult customer rule",
                List.of(
                        new ConditionDto("customer.age", "NUMBER", "GTE", 18)
                ),
                10,
                true,
                Set.of("customer", "age")
        );

        ResponseEntity<RuleDto> response = restTemplate.postForEntity(
                getBaseUrl(),
                request,
                RuleDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo("rule-1");
        assertThat(response.getBody().name()).isEqualTo("Adult customer rule");
        assertThat(response.getBody().conditions()).hasSize(1);
    }

    @Test
    void shouldGetRuleById() {
        // Create a rule
        CreateRuleRequest createRequest = new CreateRuleRequest(
                "rule-get",
                "Test rule",
                List.of(new ConditionDto("customer.age", "NUMBER", "GT", 0)),
                5,
                true,
                Set.of()
        );
        restTemplate.postForEntity(getBaseUrl(), createRequest, RuleDto.class);

        // Get it
        ResponseEntity<RuleDto> response = restTemplate.getForEntity(
                getBaseUrl() + "/rule-get",
                RuleDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo("rule-get");
    }

    @Test
    void shouldGetAllRules() {
        // Create multiple rules
        restTemplate.postForEntity(
                getBaseUrl(),
                new CreateRuleRequest("rule-all-1", "Rule 1", 
                        List.of(new ConditionDto("customer.age", "NUMBER", "GT", 0)), 
                        1, true, Set.of()),
                RuleDto.class
        );
        restTemplate.postForEntity(
                getBaseUrl(),
                new CreateRuleRequest("rule-all-2", "Rule 2",
                        List.of(new ConditionDto("order.total", "DECIMAL", "GT", 0)),
                        2, true, Set.of()),
                RuleDto.class
        );

        ResponseEntity<RuleDto[]> response = restTemplate.getForEntity(
                getBaseUrl(),
                RuleDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldGetActiveRules() {
        // Create active and inactive rules
        restTemplate.postForEntity(
                getBaseUrl(),
                new CreateRuleRequest("rule-active", "Active rule",
                        List.of(new ConditionDto("customer.age", "NUMBER", "GT", 0)),
                        1, true, Set.of()),
                RuleDto.class
        );
        restTemplate.postForEntity(
                getBaseUrl(),
                new CreateRuleRequest("rule-inactive", "Inactive rule",
                        List.of(new ConditionDto("customer.age", "NUMBER", "GT", 0)),
                        1, false, Set.of()),
                RuleDto.class
        );

        ResponseEntity<RuleDto[]> response = restTemplate.getForEntity(
                getBaseUrl() + "/active",
                RuleDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // Should only return active rules
        for (RuleDto rule : response.getBody()) {
            assertThat(rule.metadata().active()).isTrue();
        }
    }

    @Test
    void shouldUpdateRule() {
        // Create a rule
        CreateRuleRequest createRequest = new CreateRuleRequest(
                "rule-update",
                "Original name",
                List.of(new ConditionDto("customer.age", "NUMBER", "GT", 0)),
                5,
                true,
                Set.of("tag1")
        );
        restTemplate.postForEntity(getBaseUrl(), createRequest, RuleDto.class);

        // Update it
        UpdateRuleRequest updateRequest = new UpdateRuleRequest(
                "Updated name",
                List.of(new ConditionDto("customer.age", "NUMBER", "GTE", 18)),
                20,
                false,
                Set.of("tag2")
        );

        ResponseEntity<RuleDto> response = restTemplate.exchange(
                getBaseUrl() + "/rule-update",
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                RuleDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Updated name");
        assertThat(response.getBody().metadata().priority()).isEqualTo(20);
        assertThat(response.getBody().metadata().active()).isFalse();
    }

    @Test
    void shouldDeleteRule() {
        // Create a rule
        CreateRuleRequest createRequest = new CreateRuleRequest(
                "rule-delete",
                "To be deleted",
                List.of(new ConditionDto("customer.age", "NUMBER", "GT", 0)),
                1,
                true,
                Set.of()
        );
        restTemplate.postForEntity(getBaseUrl(), createRequest, RuleDto.class);

        // Delete it
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                getBaseUrl() + "/rule-delete",
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify it's deleted
        ResponseEntity<RuleDto> getResponse = restTemplate.getForEntity(
                getBaseUrl() + "/rule-delete",
                RuleDto.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnNotFoundForNonExistentRule() {
        ResponseEntity<RuleDto> response = restTemplate.getForEntity(
                getBaseUrl() + "/nonexistent",
                RuleDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}

