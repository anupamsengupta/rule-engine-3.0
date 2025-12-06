package com.ruleengine.infrastructure.strategy.expression;

import com.ruleengine.infrastructure.model.Product;

import java.math.BigDecimal;

/**
 * Wrapper class to make Product accessible to JEXL.
 */
public class ProductWrapper {
    private final Product product;

    public ProductWrapper(Product product) {
        this.product = product;
    }

    public BigDecimal getPrice() {
        return product.price();
    }
}

