package com.ruleengine.persistence.mapper;

import com.ruleengine.domain.attribute.Attribute;
import com.ruleengine.domain.operator.ComparisonOperator;
import com.ruleengine.domain.rule.Condition;
import com.ruleengine.persistence.entity.*;

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
        if (entity == null || entity.getAttribute() == null) {
            return null;
        }

        Attribute attribute = AttributeMapper.toDomain(entity.getAttribute());
        ComparisonOperator operator = ComparisonOperator.valueOf(entity.getOperator().name());
        
        // Deserialize target value based on type
        Object targetValue = deserializeValue(entity.getTargetValue(), entity.getTargetValueType());

        return new Condition(attribute, operator, targetValue);
    }

    /**
     * Converts a domain Condition to a ConditionEntity.
     */
    public static ConditionEntity toEntity(Condition domain, RuleEntity ruleEntity, int sequenceOrder) {
        if (domain == null) {
            return null;
        }

        AttributeEntity attributeEntity = AttributeMapper.toEntity(domain.attribute());
        ComparisonOperatorEntity operator = ComparisonOperatorEntity.valueOf(domain.operator().name());
        
        // Serialize target value
        Object targetValueObj = domain.targetValue();
        String targetValue = serializeValue(targetValueObj);
        String targetValueType = targetValueObj != null ? targetValueObj.getClass().getName() : "java.lang.Object";

        return new ConditionEntity(
            ruleEntity,
            attributeEntity,
            operator,
            targetValue,
            targetValueType,
            sequenceOrder
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

