package com.ruleengine.persistence.mapper;

import com.ruleengine.domain.attribute.Attribute;
import com.ruleengine.domain.operator.ComparisonOperator;
import com.ruleengine.domain.rule.Condition;
import com.ruleengine.persistence.entity.*;

import java.util.Optional;

/**
 * Mapper for converting between Condition domain model and ConditionEntity.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
public final class ConditionMapper {

    private ConditionMapper() {
        // Utility class
    }

    /**
     * Converts a ConditionEntity to a domain Condition.
     */
    public static Condition toDomain(ConditionEntity entity) {
        if (entity == null || entity.getLeftAttribute() == null) {
            return null;
        }

        Attribute leftAttribute = AttributeMapper.toDomain(entity.getLeftAttribute());
        ComparisonOperator operator = ComparisonOperator.valueOf(entity.getOperator().name());
        
        Optional<Attribute> rightAttribute = Optional.ofNullable(entity.getRightAttribute())
                .map(AttributeMapper::toDomain);
        
        Optional<Object> targetValue = Optional.empty();
        if (entity.getTargetValue() != null) {
            Object deserialized = deserializeValue(entity.getTargetValue(), entity.getTargetValueType());
            targetValue = Optional.of(deserialized);
        }

        return new Condition(
                entity.getId(),
                entity.getName(),
                leftAttribute,
                operator,
                rightAttribute,
                targetValue
        );
    }

    /**
     * Converts a domain Condition to a ConditionEntity.
     */
    public static ConditionEntity toEntity(Condition domain) {
        if (domain == null) {
            return null;
        }

        AttributeEntity leftAttributeEntity = AttributeMapper.toEntity(domain.leftAttribute());
        ComparisonOperatorEntity operator = ComparisonOperatorEntity.valueOf(domain.operator().name());
        
        AttributeEntity rightAttributeEntity = null;
        if (domain.rightAttribute().isPresent()) {
            rightAttributeEntity = AttributeMapper.toEntity(domain.rightAttribute().get());
        }
        
        String targetValue = null;
        String targetValueType = null;
        if (domain.targetValue().isPresent()) {
            Object targetValueObj = domain.targetValue().get();
            targetValue = serializeValue(targetValueObj);
            targetValueType = targetValueObj != null ? targetValueObj.getClass().getName() : "java.lang.Object";
        }

        return new ConditionEntity(
                domain.id(),
                domain.name(),
                leftAttributeEntity,
                operator,
                rightAttributeEntity,
                targetValue,
                targetValueType
        );
    }

    private static String serializeValue(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private static Object deserializeValue(String value, String type) {
        if (value == null || type == null) {
            return value;
        }

        try {
            Class<?> clazz = Class.forName(type);
            if (clazz == String.class) {
                return value;
            } else if (clazz == Integer.class || clazz == int.class) {
                return Integer.parseInt(value);
            } else if (clazz == Long.class || clazz == long.class) {
                return Long.parseLong(value);
            } else if (clazz == Double.class || clazz == double.class) {
                return Double.parseDouble(value);
            } else if (clazz == Float.class || clazz == float.class) {
                return Float.parseFloat(value);
            } else if (clazz == Boolean.class || clazz == boolean.class) {
                return Boolean.parseBoolean(value);
            }
        } catch (ClassNotFoundException e) {
            // Fall through to return as string
        }

        return value;
    }
}
