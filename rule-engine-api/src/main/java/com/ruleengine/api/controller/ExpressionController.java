package com.ruleengine.api.controller;

import com.ruleengine.api.dto.ExpressionEvaluationRequest;
import com.ruleengine.api.dto.ExpressionEvaluationResponse;
import com.ruleengine.application.service.ExpressionEngineService;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.ExpressionEvaluationException;
import com.ruleengine.domain.expression.Expression;
import com.ruleengine.domain.expression.ExpressionEvaluationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * REST controller for expression evaluation operations.
 * Thin controller that delegates to application services.
 * 
 * Module: rule-engine-api
 * Layer: API
 */
@RestController
@RequestMapping("/api/expressions")
public class ExpressionController {
    private final ExpressionEngineService expressionEngineService;

    public ExpressionController(ExpressionEngineService expressionEngineService) {
        this.expressionEngineService = expressionEngineService;
    }

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
}

