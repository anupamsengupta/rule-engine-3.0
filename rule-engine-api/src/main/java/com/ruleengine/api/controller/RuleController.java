package com.ruleengine.api.controller;

import com.ruleengine.api.dto.RuleValidationRequest;
import com.ruleengine.api.dto.RuleValidationResponse;
import com.ruleengine.application.service.RuleEngineService;
import com.ruleengine.domain.attribute.Attribute;
import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.RuleEvaluationException;
import com.ruleengine.domain.operator.ComparisonOperator;
import com.ruleengine.domain.rule.Condition;
import com.ruleengine.domain.rule.Rule;
import com.ruleengine.domain.rule.RuleMetadata;
import com.ruleengine.domain.rule.RuleValidationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for rule validation operations.
 * Thin controller that delegates to application services.
 * 
 * Module: rule-engine-api
 * Layer: API
 */
@RestController
@RequestMapping("/api/rules")
public class RuleController {
    private final RuleEngineService ruleEngineService;

    public RuleController(RuleEngineService ruleEngineService) {
        this.ruleEngineService = ruleEngineService;
    }

    @PostMapping("/validate")
    public ResponseEntity<RuleValidationResponse> validateRule(
            @RequestBody RuleValidationRequest request
    ) {
        try {
            // Map DTO to domain model
            Rule rule = mapToRule(request);
            EvaluationContext context = EvaluationContext.from(request.context());

            // Delegate to application service
            RuleValidationResult result = ruleEngineService.validateRule(rule, context);

            // Map domain result to DTO
            RuleValidationResponse response = new RuleValidationResponse(
                result.passed(),
                result.message().orElse(null),
                result.details().orElse(null)
            );

            return ResponseEntity.ok(response);
        } catch (RuleEvaluationException e) {
            return ResponseEntity.badRequest()
                .body(new RuleValidationResponse(false, e.getMessage(), null));
        }
    }

    private Rule mapToRule(RuleValidationRequest request) {
        List<Condition> conditions = request.conditions().stream()
            .map(cond -> {
                Attribute attribute = new Attribute(
                    cond.attributeCode(),
                    AttributeType.valueOf(cond.attributeType()),
                    null
                );
                return new Condition(
                    attribute,
                    ComparisonOperator.valueOf(cond.operator()),
                    cond.targetValue()
                );
            })
            .collect(Collectors.toList());

        return new Rule(
            request.ruleId(),
            request.ruleName(),
            conditions,
            RuleMetadata.defaults()
        );
    }
}

