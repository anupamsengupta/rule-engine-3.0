package com.ruleengine.api.controller;

import com.ruleengine.api.dto.*;
import com.ruleengine.application.service.RuleEngineService;
import com.ruleengine.application.service.RuleService;
import com.ruleengine.domain.attribute.Attribute;
import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.RuleEvaluationException;
import com.ruleengine.domain.operator.ComparisonOperator;
import com.ruleengine.domain.rule.Condition;
import com.ruleengine.domain.rule.Rule;
import com.ruleengine.domain.rule.RuleMetadata;
import com.ruleengine.domain.rule.RuleValidationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST controller for rule CRUD and validation operations.
 * Thin controller that delegates to application services.
 * 
 * Module: rule-engine-api
 * Layer: API
 */
@RestController
@RequestMapping("/api/rules")
public class RuleController {
    private final RuleEngineService ruleEngineService;
    private final RuleService ruleService;

    public RuleController(RuleEngineService ruleEngineService, RuleService ruleService) {
        this.ruleEngineService = ruleEngineService;
        this.ruleService = ruleService;
    }

    @PostMapping("/validate")
    public ResponseEntity<RuleValidationResponse> validateRule(
            @RequestBody RuleValidationRequest request
    ) {
        try {
            // For validation endpoint, we support inline conditions for ad-hoc validation
            // Convert inline conditions to Condition objects
            List<Condition> conditions = request.conditions().stream()
                .map(cond -> {
                    Attribute attribute = new Attribute(
                        cond.attributeCode(),
                        AttributeType.valueOf(cond.attributeType()),
                        null
                    );
                    return Condition.attributeVsValue(
                        "temp-" + java.util.UUID.randomUUID().toString(),
                        "Temporary condition",
                        attribute,
                        ComparisonOperator.valueOf(cond.operator()),
                        cond.targetValue()
                    );
                })
                .collect(Collectors.toList());
            
            // Create a temporary rule with condition IDs (not used for validation)
            Rule rule = new Rule(
                request.ruleId(),
                request.ruleName(),
                conditions.stream().map(Condition::id).collect(Collectors.toList()),
                RuleMetadata.defaults()
            );
            
            EvaluationContext context = EvaluationContext.from(request.context());

            // Delegate to application service with conditions
            RuleValidationResult result = ruleEngineService.validateRule(rule, context, conditions);

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

    // CRUD Operations

    @PostMapping
    public ResponseEntity<RuleDto> createRule(@RequestBody CreateRuleRequest request) {
        try {
            Rule rule = mapToRule(request);
            Rule created = ruleService.createRule(rule);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RuleDto> getRule(@PathVariable String id) {
        return ruleService.getRuleById(id)
                .map(rule -> ResponseEntity.ok(toDto(rule)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<RuleDto>> getAllRules() {
        List<RuleDto> rules = ruleService.getAllRules().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/active")
    public ResponseEntity<List<RuleDto>> getActiveRules() {
        List<RuleDto> rules = ruleService.getActiveRules().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/active/by-priority")
    public ResponseEntity<List<RuleDto>> getActiveRulesOrderedByPriority() {
        List<RuleDto> rules = ruleService.getActiveRulesOrderedByPriority().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/active/by-tag/{tag}")
    public ResponseEntity<List<RuleDto>> getActiveRulesByTag(@PathVariable String tag) {
        List<RuleDto> rules = ruleService.getActiveRulesByTag(tag).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rules);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RuleDto> updateRule(
            @PathVariable String id,
            @RequestBody UpdateRuleRequest request
    ) {
        try {
            Rule existing = ruleService.getRuleById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Rule not found"));
            
            Rule updated = new Rule(
                    id,
                    request.name(),
                    request.conditionIds() != null ? request.conditionIds() : existing.conditionIds(),
                    new RuleMetadata(
                            request.priority() != null ? request.priority() : existing.metadata().priority(),
                            request.active() != null ? request.active() : existing.metadata().active(),
                            request.tags() != null ? request.tags() : existing.metadata().tags()
                    )
            );
            Rule saved = ruleService.updateRule(updated);
            return ResponseEntity.ok(toDto(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable String id) {
        try {
            ruleService.deleteRule(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Helper methods


    private Rule mapToRule(CreateRuleRequest request) {
        return new Rule(
                request.id(),
                request.name(),
                request.conditionIds() != null ? request.conditionIds() : List.of(),
                new RuleMetadata(
                        request.priority() != null ? request.priority() : 0,
                        request.active() != null ? request.active() : true,
                        request.tags() != null ? request.tags() : Set.of()
                )
        );
    }

    private RuleDto toDto(Rule rule) {
        return new RuleDto(
                rule.id(),
                rule.name(),
                rule.conditionIds(),
                new RuleMetadataDto(
                        rule.metadata().priority(),
                        rule.metadata().active(),
                        rule.metadata().tags()
                )
        );
    }
}

