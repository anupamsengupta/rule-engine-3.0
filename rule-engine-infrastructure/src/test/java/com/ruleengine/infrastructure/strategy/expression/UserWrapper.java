package com.ruleengine.infrastructure.strategy.expression;

import com.ruleengine.infrastructure.model.User;

import java.math.BigDecimal;

/**
 * Wrapper class to make User accessible to JEXL.
 */
public class UserWrapper {
    private final User user;

    public UserWrapper(User user) {
        this.user = user;
    }

    public BigDecimal getLimit() {
        return user.limit();
    }
}

