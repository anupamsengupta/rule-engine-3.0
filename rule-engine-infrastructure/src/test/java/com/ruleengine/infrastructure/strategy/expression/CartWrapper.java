package com.ruleengine.infrastructure.strategy.expression;

import com.ruleengine.infrastructure.model.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Wrapper class to make ShoppingCart accessible to JEXL.
 * JEXL has issues with Java record accessors, so this wrapper provides getter methods.
 */
public class CartWrapper {
    private final ShoppingCart cart;

    public CartWrapper(ShoppingCart cart) {
        this.cart = cart;
    }

    public BigDecimal getCartTotalAmount() {
        return cart.cartTotalAmount();
    }

    public List<LineItem> getLineItems() {
        return cart.lineItems();
    }
    
    public List<LineItemWrapper> getLineItemWrappers() {
        return cart.lineItems().stream()
                .map(LineItemWrapper::new)
                .toList();
    }

    public UserWrapper getUser() {
        return new UserWrapper(cart.user());
    }
    
    public BigDecimal getUserLimit() {
        return cart.user().limit();
    }
}

