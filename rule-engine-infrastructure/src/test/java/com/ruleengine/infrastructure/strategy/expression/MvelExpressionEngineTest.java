package com.ruleengine.infrastructure.strategy.expression;

import com.ruleengine.domain.attribute.AttributeType;
import com.ruleengine.domain.context.EvaluationContext;
import com.ruleengine.domain.exception.ExpressionEvaluationException;
import com.ruleengine.domain.expression.ExpressionEvaluationResult;
import com.ruleengine.domain.factory.EngineType;
import com.ruleengine.infrastructure.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MvelExpressionEngine.
 * 
 * Module: rule-engine-infrastructure
 * Layer: Infrastructure
 */
class MvelExpressionEngineTest {

    private final MvelExpressionEngine engine = new MvelExpressionEngine();

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
    void shouldEvaluateComplexExpression() throws ExpressionEvaluationException {
        EvaluationContext context = EvaluationContext.from(Map.of("x", 10, "y", 5, "z", 3));
        ExpressionEvaluationResult result = engine.evaluate("x > y && y > z", context);

        assertThat(result.value()).isEqualTo(true);
        assertThat(result.type()).isEqualTo(AttributeType.BOOLEAN);
    }

    @Test
    void shouldEvaluateExpressionWithVariableNamesContainingDots() throws ExpressionEvaluationException {
        EvaluationContext context = EvaluationContext.from(Map.of("customer.age", 25, "minimum.age", 18));
        ExpressionEvaluationResult result = engine.evaluate("customer.age >= minimum.age", context);

        assertThat(result.value()).isEqualTo(true);
        assertThat(result.type()).isEqualTo(AttributeType.BOOLEAN);
    }

    @Test
    void shouldThrowExceptionForInvalidExpression() {
        EvaluationContext context = EvaluationContext.from(Map.of("x", 10));

        assertThatThrownBy(() -> engine.evaluate("x +", context))
            .isInstanceOf(ExpressionEvaluationException.class);
    }

    @Test
    void shouldSupportMvelEngineType() {
        assertThat(engine.supports(EngineType.MVEL)).isTrue();
        assertThat(engine.supports(EngineType.SPEL)).isFalse();
        assertThat(engine.supports(EngineType.JEXL)).isFalse();
        assertThat(engine.supports(EngineType.GROOVY)).isFalse();
    }

    // MVEL Tests with ShoppingCart model
    // MVEL can access record accessors as properties
    //private static final String MVEL_CART_TOTAL_EXPRESSION = "root.cartTotalAmount == helper.sumLineItemsTotal(root.lineItems)";
    //private static final String MVEL_USER_LIMIT_EXPRESSION = "root.cartTotalAmount.compareTo(root.user.limit) <= 0";
    /** MVEL expression that validates cart total equals sum of line item totals.
     * Each line item total is calculated as: (quantity * product.price) - appliedDiscount */
    private static final String MVEL_CART_TOTAL_EXPRESSION = "sum = new java.math.BigDecimal(\"0\"); " +
            "foreach (li : root.lineItems()) { " +
            " sum = sum.add(li.product().price().multiply(new java.math.BigDecimal(li.quantity())).subtract(li.appliedDiscount())); " +
            "} " +
            "root.cartTotalAmount().compareTo(sum) == 0";

    /** MVEL expression that validates cart total does not exceed user's limit.
     * Returns true if cartTotalAmount <= user.limit */
    private static final String MVEL_USER_LIMIT_EXPRESSION = "root.cartTotalAmount().compareTo(root.user().limit()) <= 0";
    
    @Test
    void testMvelValidCart_TotalMatchesLineItemsAndBelowLimit() throws ExpressionEvaluationException {
        ShoppingCart cart = createShoppingCart();
        BigDecimal expectedTotal = TestHelper.sumLineItemsTotal(cart.lineItems());
        cart = new ShoppingCart(cart.lineItems(), cart.user(), expectedTotal);

        // Create mutable map with root and helper object
        Map<String, Object> contextValues = new java.util.HashMap<>();
        contextValues.put("root", cart);
        contextValues.put("helper", new TestHelper());
        EvaluationContext context = EvaluationContext.from(contextValues);

        ExpressionEvaluationResult result = engine.evaluate(MVEL_CART_TOTAL_EXPRESSION, context);

        assertNotNull(result);
        assertTrue((Boolean) result.value());
    }

    @Test
    void testMvelInvalidCart_TotalMismatchesLineItems() throws ExpressionEvaluationException {
        ShoppingCart cart = createShoppingCart();
        BigDecimal expectedTotal = TestHelper.sumLineItemsTotal(cart.lineItems());
        BigDecimal wrongTotal = expectedTotal.add(new BigDecimal("10.00"));
        cart = new ShoppingCart(cart.lineItems(), cart.user(), wrongTotal);

        // Create mutable map with root and helper object
        Map<String, Object> contextValues = new java.util.HashMap<>();
        contextValues.put("root", cart);
        contextValues.put("helper", new TestHelper());
        EvaluationContext context = EvaluationContext.from(contextValues);

        ExpressionEvaluationResult result = engine.evaluate(MVEL_CART_TOTAL_EXPRESSION, context);

        assertNotNull(result);
        assertFalse((Boolean) result.value());
    }

    @Test
    void testMvelInvalidCart_TotalExceedsLimit() throws ExpressionEvaluationException {
        List<LineItem> lineItems = createShoppingCart().lineItems();
        User lowLimitUser = new User("login123", "user456", new BigDecimal("400"), UserStatus.ACTIVE, "user@example.com");
        BigDecimal expectedTotal = TestHelper.sumLineItemsTotal(lineItems);
        ShoppingCart cart = new ShoppingCart(lineItems, lowLimitUser, expectedTotal);

        // Create mutable map with root
        Map<String, Object> contextValues = new java.util.HashMap<>();
        contextValues.put("root", cart);
        EvaluationContext context = EvaluationContext.from(contextValues);

        ExpressionEvaluationResult result = engine.evaluate(MVEL_USER_LIMIT_EXPRESSION, context);

        assertNotNull(result);
        assertFalse((Boolean) result.value());
    }

    @Test
    void testMvelInvalidCart_BothConditionsFail() throws ExpressionEvaluationException {
        List<LineItem> lineItems = createShoppingCart().lineItems();
        User lowLimitUser = new User("login123", "user456", new BigDecimal("400"), UserStatus.ACTIVE, "user@example.com");
        BigDecimal expectedTotal = TestHelper.sumLineItemsTotal(lineItems);
        BigDecimal wrongTotal = expectedTotal.add(new BigDecimal("10.00"));
        ShoppingCart cart = new ShoppingCart(lineItems, lowLimitUser, wrongTotal);

        // Create mutable map with root and helper object
        Map<String, Object> contextValues = new java.util.HashMap<>();
        contextValues.put("root", cart);
        contextValues.put("helper", new TestHelper());
        EvaluationContext context = EvaluationContext.from(contextValues);

        ExpressionEvaluationResult result = engine.evaluate(MVEL_CART_TOTAL_EXPRESSION, context);
        assertNotNull(result);
        assertFalse((Boolean) result.value());

        result = engine.evaluate(MVEL_USER_LIMIT_EXPRESSION, context);
        assertNotNull(result);
        assertFalse((Boolean) result.value());
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

