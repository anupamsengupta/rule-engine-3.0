package com.ruleengine.application.service;

import com.ruleengine.domain.expression.Expression;
import com.ruleengine.persistence.entity.ExpressionEntity;
import com.ruleengine.persistence.mapper.ExpressionMapper;
import com.ruleengine.persistence.repository.ExpressionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for managing Expression entities.
 * Provides CRUD operations for expressions.
 *
 * Module: rule-engine-application
 * Layer: Application
 */
@Service
@Transactional
public class ExpressionService {
    private final ExpressionRepository expressionRepository;

    public ExpressionService(ExpressionRepository expressionRepository) {
        this.expressionRepository = expressionRepository;
    }

    /**
     * Creates a new expression.
     * If no ID is provided, generates one automatically.
     */
    public Expression createExpression(Expression expression) {
        String id = expression.id().orElse(UUID.randomUUID().toString());
        
        if (expressionRepository.existsById(id)) {
            throw new IllegalArgumentException("Expression with id '" + id + "' already exists");
        }
        
        Expression expressionWithId = new Expression(Optional.of(id), expression.expressionString());
        ExpressionEntity entity = ExpressionMapper.toEntity(expressionWithId);
        ExpressionEntity saved = expressionRepository.save(entity);
        return ExpressionMapper.toDomain(saved);
    }

    /**
     * Retrieves an expression by id.
     */
    @Transactional(readOnly = true)
    public Optional<Expression> getExpressionById(String id) {
        return expressionRepository.findById(id)
                .map(ExpressionMapper::toDomain);
    }

    /**
     * Retrieves all expressions.
     */
    @Transactional(readOnly = true)
    public List<Expression> getAllExpressions() {
        return expressionRepository.findAll().stream()
                .map(ExpressionMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing expression.
     */
    public Expression updateExpression(Expression expression) {
        String id = expression.id()
                .orElseThrow(() -> new IllegalArgumentException("Expression id is required for update"));
        
        ExpressionEntity existing = expressionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expression with id '" + id + "' not found"));
        
        existing.setExpressionString(expression.expressionString());
        
        ExpressionEntity saved = expressionRepository.save(existing);
        return ExpressionMapper.toDomain(saved);
    }

    /**
     * Deletes an expression by id.
     */
    public void deleteExpression(String id) {
        if (!expressionRepository.existsById(id)) {
            throw new IllegalArgumentException("Expression with id '" + id + "' not found");
        }
        expressionRepository.deleteById(id);
    }

    /**
     * Checks if an expression exists by id.
     */
    @Transactional(readOnly = true)
    public boolean expressionExists(String id) {
        return expressionRepository.existsById(id);
    }
}

