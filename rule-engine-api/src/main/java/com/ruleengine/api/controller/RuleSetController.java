package com.ruleengine.api.controller;

import com.ruleengine.api.dto.*;
import com.ruleengine.application.service.RuleEngineService;
import com.ruleengine.application.service.RuleSetService;
import com.ruleengine.application.service.RuleService;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.RuleEvaluationException;
import com.ruleengine.domain.factory.EngineType;
import com.ruleengine.domain.rule.Rule;
import com.ruleengine.domain.rule.RuleSet;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for RuleSet CRUD operations.
 *
 * Module: rule-engine-api
 * Layer: API
 */
@RestController
@RequestMapping("/api/rule-sets")
public class RuleSetController {
    private final RuleSetService ruleSetService;
    private final RuleService ruleService;
    private final RuleEngineService ruleEngineService;

    public RuleSetController(
            RuleSetService ruleSetService, 
            RuleService ruleService,
            RuleEngineService ruleEngineService
    ) {
        this.ruleSetService = ruleSetService;
        this.ruleService = ruleService;
        this.ruleEngineService = ruleEngineService;
    }

    @PostMapping
    public ResponseEntity<RuleSetDto> createRuleSet(@RequestBody CreateRuleSetRequest request) {
        try {
            RuleSet ruleSet = mapToRuleSet(request);
            RuleSet created = ruleSetService.createRuleSet(ruleSet);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RuleSetDto> getRuleSet(@PathVariable String id) {
        return ruleSetService.getRuleSetById(id)
                .map(rs -> ResponseEntity.ok(toDto(rs)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<RuleSetDto>> getAllRuleSets(
            @RequestParam(required = false) String category
    ) {
        List<RuleSetDto> ruleSets;
        if (category != null && !category.isBlank()) {
            ruleSets = ruleSetService.getRuleSetsByCategory(category).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        } else {
            ruleSets = ruleSetService.getAllRuleSets().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(ruleSets);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RuleSetDto> updateRuleSet(
            @PathVariable String id,
            @RequestBody UpdateRuleSetRequest request
    ) {
        try {
            RuleSet existing = ruleSetService.getRuleSetById(id)
                    .orElseThrow(() -> new IllegalArgumentException("RuleSet not found"));
            
            RuleSet updated = new RuleSet(
                    id,
                    request.name(),
                    loadRules(request.ruleIds()),
                    request.stopOnFirstFailure() != null ? request.stopOnFirstFailure() : existing.stopOnFirstFailure(),
                    request.engineType() != null ? EngineType.valueOf(request.engineType()) : existing.engineType(),
                    request.ruleCategory() != null && !request.ruleCategory().isBlank() ? request.ruleCategory() : existing.ruleCategory()
            );
            RuleSet saved = ruleSetService.updateRuleSet(updated);
            return ResponseEntity.ok(toDto(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRuleSet(@PathVariable String id) {
        try {
            ruleSetService.deleteRuleSet(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/validate-by-category")
    public ResponseEntity<CategoryValidationResponse> validateByCategory(
            @RequestBody CategoryValidationRequest request
    ) {
        try {
            if (request.ruleCategory() == null || request.ruleCategory().isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            // Load all rule sets for the category
            List<RuleSet> ruleSets = ruleSetService.getRuleSetsByCategory(request.ruleCategory());
            
            // Create evaluation context from the context map
            EvaluationContext context = EvaluationContext.from(request.contextMap() != null ? request.contextMap() : java.util.Map.of());

            // Validate all rule sets
            RuleEngineService.CategoryValidationResult result = ruleEngineService.validateRuleSetsByCategory(ruleSets, context);

            // Map to response DTO
            CategoryValidationResponse response = new CategoryValidationResponse(
                    result.passed(),
                    result.message(),
                    request.ruleCategory(),
                    ruleSets.size(),
                    (int) result.ruleSetResults().stream().filter(RuleEngineService.RuleSetValidationResult::passed).count(),
                    (int) result.ruleSetResults().stream().filter(rs -> !rs.passed()).count(),
                    result.ruleSetResults().stream()
                            .map(rs -> new CategoryValidationResponse.RuleSetValidationResult(
                                    rs.ruleSetId(),
                                    rs.ruleSetName(),
                                    rs.passed(),
                                    rs.message(),
                                    rs.ruleResults().stream()
                                            .map(r -> new CategoryValidationResponse.RuleValidationResult(
                                                    r.ruleId(),
                                                    r.ruleName(),
                                                    r.passed(),
                                                    r.message()
                                            ))
                                            .collect(Collectors.toList())
                            ))
                            .collect(Collectors.toList()),
                    result.details()
            );

            return ResponseEntity.ok(response);
        } catch (RuleEvaluationException e) {
            return ResponseEntity.badRequest()
                    .body(new CategoryValidationResponse(
                            false,
                            "Validation error: " + e.getMessage(),
                            request.ruleCategory(),
                            0,
                            0,
                            0,
                            List.of(),
                            null
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private RuleSet mapToRuleSet(CreateRuleSetRequest request) {
        if (request.ruleCategory() == null || request.ruleCategory().isBlank()) {
            throw new IllegalArgumentException("RuleSet ruleCategory cannot be null or blank");
        }
        return new RuleSet(
                request.id(),
                request.name(),
                loadRules(request.ruleIds()),
                request.stopOnFirstFailure() != null ? request.stopOnFirstFailure() : false,
                request.engineType() != null ? EngineType.valueOf(request.engineType()) : EngineType.SPEL,
                request.ruleCategory()
        );
    }

    private List<Rule> loadRules(List<String> ruleIds) {
        return ruleIds.stream()
                .map(ruleId -> ruleService.getRuleById(ruleId)
                        .orElseThrow(() -> new IllegalArgumentException("Rule with id '" + ruleId + "' not found")))
                .collect(Collectors.toList());
    }

    private RuleSetDto toDto(RuleSet ruleSet) {
        return new RuleSetDto(
                ruleSet.id(),
                ruleSet.name(),
                ruleSet.rules().stream()
                        .map(Rule::id)
                        .collect(Collectors.toList()),
                ruleSet.stopOnFirstFailure(),
                ruleSet.engineType().name(),
                ruleSet.ruleCategory()
        );
    }
}

