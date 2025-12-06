package com.ruleengine.infrastructure.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a shopping cart containing line items and a user.
 */
public record ShoppingCart(List<LineItem> lineItems, User user, BigDecimal cartTotalAmount) {
}
