package com.ruleengine.app.integration;

import com.ruleengine.api.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end test for Rule Engine API.
 * Tests the complete flow from REST API to domain validation.
 * 
 * Module: rule-engine-app
 * Layer: App (E2E Tests)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb-RuleEngineE2ETest;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
class RuleEngineE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String uuid_exec;

    @BeforeEach
    void setUp() {
        uuid_exec = java.util.UUID.randomUUID().toString();
        // Create attributes needed for rules
        createAttribute("customer.age" + uuid_exec, "NUMBER");
        createAttribute("order.total" + uuid_exec, "DECIMAL");
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

    private String createCondition(String id, String name, String leftAttrCode, String operator, Object targetValue) {
        CreateConditionRequest condRequest = new CreateConditionRequest(
                id,
                name,
                leftAttrCode,
                operator,
                null,
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
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void shouldValidateRuleViaApi() {
        RuleValidationRequest request = new RuleValidationRequest(
            "rule-1",
            "Adult customer rule",
            List.of(
                new RuleValidationRequest.ConditionDto(
                    "customer.age",
                    "NUMBER",
                    "GTE",
                    18
                )
            ),
            Map.of("customer.age", 25)
        );

        ResponseEntity<RuleValidationResponse> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/rules/validate",
            request,
            RuleValidationResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().passed()).isTrue();
    }

    @Test
    void shouldReturnFailureForInvalidRule() {
        RuleValidationRequest request = new RuleValidationRequest(
            "rule-1",
            "Adult customer rule",
            List.of(
                new RuleValidationRequest.ConditionDto(
                    "customer.age",
                    "NUMBER",
                    "GTE",
                    18
                )
            ),
            Map.of("customer.age", 15) // Age below threshold
        );

        ResponseEntity<RuleValidationResponse> response = restTemplate.postForEntity(
            "http://localhost:" + port + "/api/rules/validate",
            request,
            RuleValidationResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().passed()).isFalse();
    }

    @Test
    void shouldValidateRuleSetsByCategory_AllPass() {
        // Setup: Create conditions, rules, and rule sets for a category
        String cond1 = createCondition("cond-age" + uuid_exec, "Age check", 
                "customer.age" + uuid_exec, "GTE", 18);
        String cond2 = createCondition("cond-total" + uuid_exec, "Total check", 
                "order.total" + uuid_exec, "GT", 100);
        
        String rule1 = "rule-age" + uuid_exec;
        String rule2 = "rule-total" + uuid_exec;
        createRule(rule1, "Age validation rule", cond1);
        createRule(rule2, "Total validation rule", cond2);
        
        // Create rule sets for the category
        CreateRuleSetRequest ruleSet1 = new CreateRuleSetRequest(
                "ruleset-1" + uuid_exec,
                "Age validation rule set",
                List.of(rule1),
                false,
                "SPEL",
                "Validation"
        );
        CreateRuleSetRequest ruleSet2 = new CreateRuleSetRequest(
                "ruleset-2" + uuid_exec,
                "Total validation rule set",
                List.of(rule2),
                false,
                "SPEL",
                "Validation"
        );
        
        restTemplate.postForEntity(
                "http://localhost:" + port + "/api/rule-sets",
                ruleSet1,
                RuleSetDto.class
        );
        restTemplate.postForEntity(
                "http://localhost:" + port + "/api/rule-sets",
                ruleSet2,
                RuleSetDto.class
        );

        // Test: Validate by category with context that passes all rules
        CategoryValidationRequest request = new CategoryValidationRequest(
                "Validation",
                Map.of(
                        "customer.age" + uuid_exec, 25,
                        "order.total" + uuid_exec, 150.00
                )
        );

        ResponseEntity<CategoryValidationResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/rule-sets/validate-by-category",
                request,
                CategoryValidationResponse.class
        );

        // Assertions
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().passed()).isTrue();
        assertThat(response.getBody().ruleCategory()).isEqualTo("Validation");
        assertThat(response.getBody().totalRuleSets()).isEqualTo(2);
        assertThat(response.getBody().passedRuleSets()).isEqualTo(2);
        assertThat(response.getBody().failedRuleSets()).isEqualTo(0);
        assertThat(response.getBody().ruleSetResults()).hasSize(2);
        assertThat(response.getBody().ruleSetResults()).allMatch(rs -> rs.passed());
    }

    @Test
    void shouldValidateRuleSetsByCategory_OneFails() {
        // Setup: Create conditions, rules, and rule sets for a category
        String cond1 = createCondition("cond-age-fail" + uuid_exec, "Age check", 
                "customer.age" + uuid_exec, "GTE", 18);
        String cond2 = createCondition("cond-total-fail" + uuid_exec, "Total check", 
                "order.total" + uuid_exec, "GT", 100);
        
        String rule1 = "rule-age-fail" + uuid_exec;
        String rule2 = "rule-total-fail" + uuid_exec;
        createRule(rule1, "Age validation rule", cond1);
        createRule(rule2, "Total validation rule", cond2);
        
        // Create rule sets for the category
        CreateRuleSetRequest ruleSet1 = new CreateRuleSetRequest(
                "ruleset-1-fail" + uuid_exec,
                "Age validation rule set",
                List.of(rule1),
                false,
                "SPEL",
                "Pricing"
        );
        CreateRuleSetRequest ruleSet2 = new CreateRuleSetRequest(
                "ruleset-2-fail" + uuid_exec,
                "Total validation rule set",
                List.of(rule2),
                false,
                "SPEL",
                "Pricing"
        );
        
        restTemplate.postForEntity(
                "http://localhost:" + port + "/api/rule-sets",
                ruleSet1,
                RuleSetDto.class
        );
        restTemplate.postForEntity(
                "http://localhost:" + port + "/api/rule-sets",
                ruleSet2,
                RuleSetDto.class
        );

        // Test: Validate by category with context where one rule fails (age is below threshold)
        CategoryValidationRequest request = new CategoryValidationRequest(
                "Pricing",
                Map.of(
                        "customer.age" + uuid_exec, 15, // Fails: below 18
                        "order.total" + uuid_exec, 150.00 // Passes: above 100
                )
        );

        ResponseEntity<CategoryValidationResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/rule-sets/validate-by-category",
                request,
                CategoryValidationResponse.class
        );

        // Assertions: Overall should fail (AND operation)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().passed()).isFalse(); // AND: all must pass
        assertThat(response.getBody().ruleCategory()).isEqualTo("Pricing");
        assertThat(response.getBody().totalRuleSets()).isEqualTo(2);
        assertThat(response.getBody().failedRuleSets()).isGreaterThan(0);
        assertThat(response.getBody().ruleSetResults()).hasSize(2);
        // At least one rule set should have failed
        assertThat(response.getBody().ruleSetResults().stream()
                .anyMatch(rs -> !rs.passed())).isTrue();
    }

    @Test
    void shouldValidateRuleSetsByCategory_EmptyCategory() {
        // Test: Validate by category that has no rule sets
        CategoryValidationRequest request = new CategoryValidationRequest(
                "NonExistentCategory",
                Map.of("customer.age" + uuid_exec, 25)
        );

        ResponseEntity<CategoryValidationResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/rule-sets/validate-by-category",
                request,
                CategoryValidationResponse.class
        );

        // Assertions: Empty category should return passed=true (no rules to fail)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().passed()).isTrue();
        assertThat(response.getBody().ruleCategory()).isEqualTo("NonExistentCategory");
        assertThat(response.getBody().totalRuleSets()).isEqualTo(0);
        assertThat(response.getBody().passedRuleSets()).isEqualTo(0);
        assertThat(response.getBody().failedRuleSets()).isEqualTo(0);
    }
}

