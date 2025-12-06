package com.ruleengine.infrastructure.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a line item in a shopping cart.
 */
public record LineItem(String id, Product product, int quantity, BigDecimal appliedDiscount) {
}
