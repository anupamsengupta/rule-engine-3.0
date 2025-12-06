package com.ruleengine.infrastructure.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a product in the system.
 */
public record Product(String productId, String description, ProductCategory category, BigDecimal price) {
}
