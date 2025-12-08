package com.ruleengine.api.controller;

import com.ruleengine.api.dto.*;
import com.ruleengine.application.service.ConditionService;
import com.ruleengine.application.service.AttributeService;
import com.ruleengine.domain.attribute.Attribute;
import com.ruleengine.domain.operator.ComparisonOperator;
import com.ruleengine.domain.rule.Condition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for Condition CRUD operations.
 *
 * Module: rule-engine-api
 * Layer: API
 */
@RestController
@RequestMapping("/api/conditions")
public class ConditionController {
    private final ConditionService conditionService;
    private final AttributeService attributeService;

    public ConditionController(ConditionService conditionService, AttributeService attributeService) {
        this.conditionService = conditionService;
        this.attributeService = attributeService;
    }

    @PostMapping
    public ResponseEntity<ConditionDto> createCondition(@RequestBody CreateConditionRequest request) {
        try {
            Attribute leftAttribute = attributeService.getAttributeByCode(request.leftAttributeCode())
                    .orElseThrow(() -> new IllegalArgumentException("Left attribute not found: " + request.leftAttributeCode()));
            
            Condition condition;
            if (request.rightAttributeCode() != null && !request.rightAttributeCode().isBlank()) {
                // Attribute vs Attribute
                Attribute rightAttribute = attributeService.getAttributeByCode(request.rightAttributeCode())
                        .orElseThrow(() -> new IllegalArgumentException("Right attribute not found: " + request.rightAttributeCode()));
                condition = Condition.attributeVsAttribute(
                        request.id(),
                        request.name(),
                        leftAttribute,
                        ComparisonOperator.valueOf(request.operator()),
                        rightAttribute
                );
            } else if (request.targetValue() != null) {
                // Attribute vs Value
                condition = Condition.attributeVsValue(
                        request.id(),
                        request.name(),
                        leftAttribute,
                        ComparisonOperator.valueOf(request.operator()),
                        request.targetValue()
                );
            } else {
                return ResponseEntity.badRequest().build();
            }
            
            Condition created = conditionService.createCondition(condition);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConditionDto> getCondition(@PathVariable String id) {
        return conditionService.getConditionById(id)
                .map(cond -> ResponseEntity.ok(toDto(cond)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ConditionDto>> getAllConditions() {
        List<ConditionDto> conditions = conditionService.getAllConditions().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(conditions);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConditionDto> updateCondition(
            @PathVariable String id,
            @RequestBody UpdateConditionRequest request
    ) {
        try {
            Condition existing = conditionService.getConditionById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Condition not found"));
            
            Attribute leftAttribute = attributeService.getAttributeByCode(request.leftAttributeCode())
                    .orElseThrow(() -> new IllegalArgumentException("Left attribute not found: " + request.leftAttributeCode()));
            
            Condition updated;
            if (request.rightAttributeCode() != null && !request.rightAttributeCode().isBlank()) {
                // Attribute vs Attribute
                Attribute rightAttribute = attributeService.getAttributeByCode(request.rightAttributeCode())
                        .orElseThrow(() -> new IllegalArgumentException("Right attribute not found: " + request.rightAttributeCode()));
                updated = Condition.attributeVsAttribute(
                        id,
                        request.name(),
                        leftAttribute,
                        ComparisonOperator.valueOf(request.operator()),
                        rightAttribute
                );
            } else if (request.targetValue() != null) {
                // Attribute vs Value
                updated = Condition.attributeVsValue(
                        id,
                        request.name(),
                        leftAttribute,
                        ComparisonOperator.valueOf(request.operator()),
                        request.targetValue()
                );
            } else {
                return ResponseEntity.badRequest().build();
            }
            
            Condition saved = conditionService.updateCondition(updated);
            return ResponseEntity.ok(toDto(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCondition(@PathVariable String id) {
        try {
            conditionService.deleteCondition(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private ConditionDto toDto(Condition condition) {
        return new ConditionDto(
                condition.id(),
                condition.name(),
                condition.leftAttribute().code(),
                condition.operator().name(),
                condition.rightAttribute().map(attr -> attr.code()).orElse(null),
                condition.targetValue().orElse(null)
        );
    }
}

