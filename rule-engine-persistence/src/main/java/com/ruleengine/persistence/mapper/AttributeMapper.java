package com.ruleengine.persistence.mapper;

import com.ruleengine.domain.attribute.Attribute;
import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.persistence.entity.AttributeEntity;
import com.ruleengine.persistence.entity.AttributeTypeEntity;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Attribute domain model and AttributeEntity.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence
 */
public final class AttributeMapper {

    private AttributeMapper() {
        // Utility class
    }

    /**
     * Converts an AttributeEntity to a domain Attribute.
     */
    public static Attribute toDomain(AttributeEntity entity) {
        if (entity == null) {
            return null;
        }

        Map<String, Object> constraints = null;
        if (entity.getConstraints() != null) {
            constraints = entity.getConstraints().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        return new Attribute(
            entity.getCode(),
            toDomainType(entity.getType()),
            Optional.ofNullable(entity.getDescription()),
            Optional.ofNullable(constraints)
        );
    }

    /**
     * Converts a domain Attribute to an AttributeEntity.
     */
    public static AttributeEntity toEntity(Attribute domain) {
        if (domain == null) {
            return null;
        }

        Map<String, String> constraints = null;
        if (domain.constraints().isPresent()) {
            constraints = domain.constraints().get().entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue() != null ? entry.getValue().toString() : null
                ));
        }

        return new AttributeEntity(
            domain.code(),
            toEntityType(domain.type()),
            domain.description().orElse(null),
            constraints
        );
    }

    private static AttributeType toDomainType(AttributeTypeEntity entityType) {
        return AttributeType.valueOf(entityType.name());
    }

    private static AttributeTypeEntity toEntityType(AttributeType domainType) {
        return AttributeTypeEntity.valueOf(domainType.name());
    }
}

