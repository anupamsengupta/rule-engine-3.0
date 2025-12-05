package com.ruleengine.persistence.mapper;

import com.ruleengine.domain.expression.Expression;
import com.ruleengine.persistence.entity.ExpressionEntity;

import java.util.Optional;

/**
 * Mapper for converting between Expression domain model and ExpressionEntity.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
public final class ExpressionMapper {

    private ExpressionMapper() {
        // Utility class
    }

    /**
     * Converts an ExpressionEntity to a domain Expression.
     */
    public static Expression toDomain(ExpressionEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Expression(
            Optional.of(entity.getId()),
            entity.getExpressionString()
        );
    }

    /**
     * Converts a domain Expression to an ExpressionEntity.
     */
    public static ExpressionEntity toEntity(Expression domain) {
        if (domain == null) {
            return null;
        }

        return new ExpressionEntity(
            domain.id().orElse(null),
            domain.expressionString(),
            null // Description not in domain model
        );
    }
}

