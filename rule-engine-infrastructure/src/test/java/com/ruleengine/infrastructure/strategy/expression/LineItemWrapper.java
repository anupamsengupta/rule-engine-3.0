package com.ruleengine.infrastructure.strategy.expression;

import com.ruleengine.infrastructure.model.LineItem;
import com.ruleengine.infrastructure.model.Product;

import java.math.BigDecimal;

/**
 * Wrapper class to make LineItem accessible to JEXL.
 */
public class LineItemWrapper {
    private final LineItem lineItem;

    public LineItemWrapper(LineItem lineItem) {
        this.lineItem = lineItem;
    }

    public ProductWrapper getProduct() {
        return new ProductWrapper(lineItem.product());
    }

    public int getQuantity() {
        return lineItem.quantity();
    }

    public BigDecimal getAppliedDiscount() {
        return lineItem.appliedDiscount();
    }
}

