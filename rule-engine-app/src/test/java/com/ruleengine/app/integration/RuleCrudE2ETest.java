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
    "spring.datasource.url=jdbc:h2:mem:testdb-RuleCrudE2ETest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
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

    @Test
    void shouldCreateRule() {
        // First create a condition
        String conditionId = createCondition("cond-1", "Age check", "customer.age", "GTE", null, 18);
        
        CreateRuleRequest request = new CreateRuleRequest(
                "rule-1",
                "Adult customer rule",
                List.of(conditionId),
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
        assertThat(response.getBody().conditionIds()).hasSize(1);
        assertThat(response.getBody().conditionIds()).contains(conditionId);
    }

    @Test
    void shouldGetRuleById() {
        // Create a condition
        String conditionId = createCondition("cond-get", "Age check", "customer.age", "GT", null, 0);
        
        // Create a rule
        CreateRuleRequest createRequest = new CreateRuleRequest(
                "rule-get",
                "Test rule",
                List.of(conditionId),
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
        assertThat(response.getBody().conditionIds()).contains(conditionId);
    }

    @Test
    void shouldGetAllRules() {
        // Create conditions
        String cond1 = createCondition("cond-all-1", "Age check 1", "customer.age", "GT", null, 0);
        String cond2 = createCondition("cond-all-2", "Total check", "order.total", "GT", null, 0);
        
        // Create multiple rules
        restTemplate.postForEntity(
                getBaseUrl(),
                new CreateRuleRequest("rule-all-1", "Rule 1", 
                        List.of(cond1), 
                        1, true, Set.of()),
                RuleDto.class
        );
        restTemplate.postForEntity(
                getBaseUrl(),
                new CreateRuleRequest("rule-all-2", "Rule 2",
                        List.of(cond2),
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
        // Create conditions
        String cond1 = createCondition("cond-active-1", "Age check", "customer.age", "GT", null, 0);
        String cond2 = createCondition("cond-active-2", "Age check 2", "customer.age", "GT", null, 0);
        
        // Create active and inactive rules
        restTemplate.postForEntity(
                getBaseUrl(),
                new CreateRuleRequest("rule-active", "Active rule",
                        List.of(cond1),
                        1, true, Set.of()),
                RuleDto.class
        );
        restTemplate.postForEntity(
                getBaseUrl(),
                new CreateRuleRequest("rule-inactive", "Inactive rule",
                        List.of(cond2),
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
        // Create conditions
        String cond1 = createCondition("cond-update-1", "Age check", "customer.age", "GT", null, 0);
        String cond2 = createCondition("cond-update-2", "Age check updated", "customer.age", "GTE", null, 18);
        
        // Create a rule
        CreateRuleRequest createRequest = new CreateRuleRequest(
                "rule-update",
                "Original name",
                List.of(cond1),
                5,
                true,
                Set.of("original")
        );
        restTemplate.postForEntity(getBaseUrl(), createRequest, RuleDto.class);

        // Update it
        UpdateRuleRequest updateRequest = new UpdateRuleRequest(
                "Updated name",
                List.of(cond2),
                20,
                false,
                Set.of("updated")
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
        assertThat(response.getBody().conditionIds()).contains(cond2);
        assertThat(response.getBody().metadata().priority()).isEqualTo(20);
        assertThat(response.getBody().metadata().active()).isFalse();
    }

    @Test
    void shouldDeleteRule() {
        // Create a condition
        String cond1 = createCondition("cond-delete", "Age check", "customer.age", "GT", null, 0);
        
        // Create a rule
        CreateRuleRequest createRequest = new CreateRuleRequest(
                "rule-delete",
                "To be deleted",
                List.of(cond1),
                1,
                true,
                Set.of()
        );
        restTemplate.postForEntity(getBaseUrl(), createRequest, RuleDto.class);

        // Delete it
        ResponseEntity<Void> response = restTemplate.exchange(
                getBaseUrl() + "/rule-delete",
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify it's deleted
        ResponseEntity<RuleDto> getResponse = restTemplate.getForEntity(
                getBaseUrl() + "/rule-delete",
                RuleDto.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldGetActiveRulesByTag() {
        // Create conditions
        String cond1 = createCondition("cond-tag-1", "Age check", "customer.age", "GT", null, 0);
        String cond2 = createCondition("cond-tag-2", "Total check", "order.total", "GT", null, 0);
        
        // Create rules with different tags
        restTemplate.postForEntity(
                getBaseUrl(),
                new CreateRuleRequest("rule-tag-1", "Tagged rule 1",
                        List.of(cond1),
                        1, true, Set.of("customer")),
                RuleDto.class
        );
        restTemplate.postForEntity(
                getBaseUrl(),
                new CreateRuleRequest("rule-tag-2", "Tagged rule 2",
                        List.of(cond2),
                        1, true, Set.of("order")),
                RuleDto.class
        );

        ResponseEntity<RuleDto[]> response = restTemplate.getForEntity(
                getBaseUrl() + "/active/by-tag/customer",
                RuleDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // Should only return rules with "customer" tag
        for (RuleDto rule : response.getBody()) {
            assertThat(rule.metadata().tags()).contains("customer");
        }
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
