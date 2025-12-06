package com.ruleengine.infrastructure.strategy.expression;

import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.ExpressionEvaluationException;
import com.ruleengine.domain.expression.ExpressionEvaluationResult;
import com.ruleengine.domain.factory.EngineType;
import com.ruleengine.infrastructure.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SpelExpressionEngine.
 * 
 * Module: rule-engine-infrastructure
 * Layer: Infrastructure
 */
class SpelExpressionEngineTest {

    private final SpelExpressionEngine engine = new SpelExpressionEngine();

    private static final String SPEL_CART_TOTAL_EXPRESSION = "root.cartTotalAmount == T(com.ruleengine.infrastructure.strategy.expression.SpelExpressionEngineTest).sumLineItemsTotal(root.lineItems)";
    private static final String SPEL_USER_LIMIT_EXPRESSION = "root.cartTotalAmount.compareTo(root.user.limit) <= 0";

    public static BigDecimal sumLineItemsTotal(List<LineItem> lineItems) {
        return lineItems.stream()
                .map(li -> li.product().price().multiply(BigDecimal.valueOf(li.quantity())).subtract(li.appliedDiscount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Test
    void shouldEvaluateSimpleExpression() throws ExpressionEvaluationException {
        EvaluationContext context = EvaluationContext.from(Map.of("x", 10, "y", 5));
        ExpressionEvaluationResult result = engine.evaluate("x > y", context);

        assertThat(result.value()).isEqualTo(true);
        assertThat(result.type()).isEqualTo(AttributeType.BOOLEAN);
        assertThat(result.error()).isEmpty();
    }


    @Test
    void shouldEvaluateArithmeticExpression() throws ExpressionEvaluationException {
        EvaluationContext context = EvaluationContext.from(Map.of("a_var", 10, "b_var", 5));
        ExpressionEvaluationResult result = engine.evaluate("a_var + b_var", context);

        assertThat(result.value()).isEqualTo(15);
        assertThat(result.type()).isEqualTo(AttributeType.NUMBER);
    }

    @Test
    void shouldThrowExceptionForInvalidExpression() {
        EvaluationContext context = EvaluationContext.from(Map.of("x", 10));

        assertThatThrownBy(() -> engine.evaluate("x +", context))
            .isInstanceOf(ExpressionEvaluationException.class);
    }

    @Test
    void shouldSupportSpelEngineType() {
        assertThat(engine.supports(EngineType.SPEL)).isTrue();
        assertThat(engine.supports(EngineType.GROOVY)).isFalse();
    }

    // SPEL Tests (direct evaluation)
    @Test
    void testSpelValidCart_TotalMatchesLineItemsAndBelowLimit() throws ExpressionEvaluationException {
        ShoppingCart cart = createShoppingCart();
        BigDecimal expectedTotal = sumLineItemsTotal(cart.lineItems());
        cart = new ShoppingCart(cart.lineItems(), cart.user(), expectedTotal);

        // First test simple property access
        EvaluationContext context = EvaluationContext.from(Map.of("root", cart));

        // Now test the full expression
        ExpressionEvaluationResult result = engine.evaluate(SPEL_CART_TOTAL_EXPRESSION, context);

        assertNotNull(result);
        assertTrue((Boolean)result.value());
    }

    @Test
    void testSpelInvalidCart_TotalMismatchesLineItems() throws ExpressionEvaluationException {
        ShoppingCart cart = createShoppingCart();
        BigDecimal expectedTotal = sumLineItemsTotal(cart.lineItems());
        BigDecimal wrongTotal = expectedTotal.add(new BigDecimal("10.00"));
        cart = new ShoppingCart(cart.lineItems(), cart.user(), wrongTotal);

        // First test simple property access
        EvaluationContext context = EvaluationContext.from(Map.of("root", cart));

        // Now test the full expression
        ExpressionEvaluationResult result = engine.evaluate(SPEL_CART_TOTAL_EXPRESSION, context);

        assertNotNull(result);
        assertFalse((Boolean)result.value());
    }

    @Test
    void testSpelInvalidCart_TotalExceedsLimit() throws ExpressionEvaluationException {
        List<LineItem> lineItems = createShoppingCart().lineItems();
        User lowLimitUser = new User("login123", "user456", new BigDecimal("400"), UserStatus.ACTIVE, "user@example.com");
        BigDecimal expectedTotal = sumLineItemsTotal(lineItems);
        ShoppingCart cart = new ShoppingCart(lineItems, lowLimitUser, expectedTotal);

        // First test simple property access
        EvaluationContext context = EvaluationContext.from(Map.of("root", cart));

        // Now test the full expression
        ExpressionEvaluationResult result = engine.evaluate(SPEL_USER_LIMIT_EXPRESSION, context);

        assertNotNull(result);
        assertFalse((Boolean)result.value());
    }

    @Test
    void testSpelInvalidCart_BothConditionsFail() throws ExpressionEvaluationException {
        List<LineItem> lineItems = createShoppingCart().lineItems();
        User lowLimitUser = new User("login123", "user456", new BigDecimal("400"), UserStatus.ACTIVE, "user@example.com");
        BigDecimal expectedTotal = sumLineItemsTotal(lineItems);
        BigDecimal wrongTotal = expectedTotal.add(new BigDecimal("10.00"));
        ShoppingCart cart = new ShoppingCart(lineItems, lowLimitUser, wrongTotal);

        // First test simple property access
        EvaluationContext context = EvaluationContext.from(Map.of("root", cart));

        // Now test the full expression
        ExpressionEvaluationResult result = engine.evaluate(SPEL_CART_TOTAL_EXPRESSION, context);
        assertNotNull(result);
        assertFalse((Boolean)result.value());

        // Now test the full expression
        result = engine.evaluate(SPEL_USER_LIMIT_EXPRESSION, context);
        assertNotNull(result);
        assertFalse((Boolean)result.value());
    }

    /** Helper method to create a shopping cart with test data (includes discount on one line item). */
    private ShoppingCart createShoppingCart() {
        // Create products
        Product laptop = new Product("P001", "Laptop", ProductCategory.ELECTRONICS, new BigDecimal("999.99"));
        Product mouse = new Product("P002", "Wireless Mouse", ProductCategory.ELECTRONICS, new BigDecimal("29.99"));
        Product coffee = new Product("P003", "Coffee Beans", ProductCategory.CONSUMABLES, new BigDecimal("12.50"));

        // Create line items
        List<LineItem> lineItems = new ArrayList<>();
        lineItems.add(new LineItem("LI001", laptop, 1, BigDecimal.ZERO));
        lineItems.add(new LineItem("LI002", mouse, 2, new BigDecimal("5.00"))); // Discount applied
        lineItems.add(new LineItem("LI003", coffee, 3, BigDecimal.ZERO));

        // Create user
        User user = new User("LOGIN001", "USER001", new BigDecimal("5000.00"), UserStatus.ACTIVE, "user@example.com");

        // Create shopping cart (cartTotalAmount will be set in tests)
        return new ShoppingCart(lineItems, user, BigDecimal.ZERO);
    }

}

