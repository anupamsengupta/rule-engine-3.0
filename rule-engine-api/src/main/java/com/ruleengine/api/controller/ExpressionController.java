package com.ruleengine.api.controller;

import com.ruleengine.api.dto.*;
import com.ruleengine.application.service.ExpressionEngineService;
import com.ruleengine.application.service.ExpressionService;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.ExpressionEvaluationException;
import com.ruleengine.domain.expression.Expression;
import com.ruleengine.domain.expression.ExpressionEvaluationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for expression CRUD and evaluation operations.
 * Thin controller that delegates to application services.
 * 
 * Module: rule-engine-api
 * Layer: API
 */
@RestController
@RequestMapping("/api/expressions")
public class ExpressionController {
    private final ExpressionEngineService expressionEngineService;
    private final ExpressionService expressionService;

    public ExpressionController(
            ExpressionEngineService expressionEngineService,
            ExpressionService expressionService
    ) {
        this.expressionEngineService = expressionEngineService;
        this.expressionService = expressionService;
    }

    // CRUD Operations

    @PostMapping
    public ResponseEntity<ExpressionDto> createExpression(@RequestBody CreateExpressionRequest request) {
        try {
            Expression expression = new Expression(
                    request.id() != null ? Optional.of(request.id()) : Optional.empty(),
                    request.expressionString()
            );
            Expression created = expressionService.createExpression(expression);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpressionDto> getExpression(@PathVariable String id) {
        return expressionService.getExpressionById(id)
                .map(expr -> ResponseEntity.ok(toDto(expr)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ExpressionDto>> getAllExpressions() {
        List<ExpressionDto> expressions = expressionService.getAllExpressions().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(expressions);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpressionDto> updateExpression(
            @PathVariable String id,
            @RequestBody UpdateExpressionRequest request
    ) {
        try {
            Expression existing = expressionService.getExpressionById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Expression not found"));
            
            Expression updated = new Expression(
                    Optional.of(id),
                    request.expressionString()
            );
            Expression saved = expressionService.updateExpression(updated);
            return ResponseEntity.ok(toDto(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpression(@PathVariable String id) {
        try {
            expressionService.deleteExpression(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Evaluation Operations (existing)

    @PostMapping("/evaluate")
    public ResponseEntity<ExpressionEvaluationResponse> evaluateExpression(
            @RequestBody ExpressionEvaluationRequest request
    ) {
        try {
            // Map DTO to domain model
            Expression expression;
            if (request.expressionId() != null && !request.expressionId().isBlank()) {
                expression = new Expression(Optional.of(request.expressionId()), request.expressionString());
            } else {
                expression = new Expression(request.expressionString());
            }
            
            EvaluationContext context = EvaluationContext.from(request.context());

            // Delegate to application service
            ExpressionEvaluationResult result = expressionEngineService.evaluateExpression(expression, context);

            // Map domain result to DTO
            ExpressionEvaluationResponse response = new ExpressionEvaluationResponse(
                result.value(),
                result.type().name(),
                result.error().orElse(null)
            );

            return ResponseEntity.ok(response);
        } catch (ExpressionEvaluationException e) {
            return ResponseEntity.badRequest()
                .body(new ExpressionEvaluationResponse(null, null, e.getMessage()));
        }
    }

    // Helper methods

    private ExpressionDto toDto(Expression expression) {
        return new ExpressionDto(
                expression.id().orElse(null),
                expression.expressionString(),
                null // Description not in domain model
        );
    }
}

