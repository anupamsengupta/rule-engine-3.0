package com.ruleengine.infrastructure.strategy.expression;

import com.ruleengine.infrastructure.model.LineItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * Helper class for test expressions that need to call methods.
 * Used by JEXL and MVEL tests.
 */
public class TestHelper {
    
    public static BigDecimal sumLineItemsTotal(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(li -> li.product().price().multiply(BigDecimal.valueOf(li.quantity())).subtract(li.appliedDiscount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // Instance method for JEXL compatibility
    public BigDecimal calculateSum(List<LineItem> lineItems) {
        return sumLineItemsTotal(lineItems);
    }
}

