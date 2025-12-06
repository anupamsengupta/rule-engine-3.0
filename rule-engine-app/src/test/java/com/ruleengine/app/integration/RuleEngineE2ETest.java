package com.ruleengine.app.integration;

import com.ruleengine.api.dto.RuleValidationRequest;
import com.ruleengine.api.dto.RuleValidationResponse;
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
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class RuleEngineE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

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
}

