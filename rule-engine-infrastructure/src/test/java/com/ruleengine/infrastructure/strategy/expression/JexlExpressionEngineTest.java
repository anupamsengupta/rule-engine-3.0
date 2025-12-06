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
 * Unit tests for JexlExpressionEngine.
 * 
 * Module: rule-engine-infrastructure
 * Layer: Infrastructure
 */
class JexlExpressionEngineTest {

    private final JexlExpressionEngine engine = new JexlExpressionEngine();

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
    void shouldSupportJexlEngineType() {
        assertThat(engine.supports(EngineType.JEXL)).isTrue();
        assertThat(engine.supports(EngineType.SPEL)).isFalse();
        assertThat(engine.supports(EngineType.MVEL)).isFalse();
        assertThat(engine.supports(EngineType.GROOVY)).isFalse();
    }

    // JEXL Tests with ShoppingCart model
    // JEXL requires explicit method calls for record accessors (records expose accessor methods, not properties)
    //private static final String JEXL_CART_TOTAL_EXPRESSION = "root.cartTotalAmount() == helper.sumLineItemsTotal(root.lineItems())";
    //private static final String JEXL_USER_LIMIT_EXPRESSION = "root.cartTotalAmount().compareTo(root.user().limit()) <= 0";
    /** JEXL expression that validates cart total equals sum of line item totals.
     * Each line item total is calculated as: (quantity * product.price) - appliedDiscount
     * Uses LineItemWrapper to provide getter methods that JEXL can access
     * Calculates sum inline using JEXL script syntax */
    private static final String JEXL_CART_TOTAL_EXPRESSION = 
            "var total = cart.getCartTotalAmount(); " +
            "var sum = new java.math.BigDecimal('0'); " +
            "for (var li : cart.getLineItemWrappers()) { " +
            "  var itemTotal = li.getProduct().getPrice().multiply(new java.math.BigDecimal(li.getQuantity())).subtract(li.getAppliedDiscount()); " +
            "  sum = sum.add(itemTotal); " +
            "} " +
            "var cmp = total.compareTo(sum); " +
            "cmp == 0";

    /** JEXL expression that validates cart total does not exceed user's limit.
     * Returns true if cartTotalAmount <= user.limit
     * Uses direct BigDecimal comparison */
    private static final String JEXL_USER_LIMIT_EXPRESSION = 
            "var total = cart.getCartTotalAmount(); " +
            "var limit = cart.getUserLimit(); " +
            "total != null && limit != null && total.compareTo(limit) <= 0";

    @Test
    void testJexlValidCart_TotalMatchesLineItemsAndBelowLimit() throws ExpressionEvaluationException {
        ShoppingCart cart = createShoppingCart();
        BigDecimal expectedTotal = TestHelper.sumLineItemsTotal(cart.lineItems());
        cart = new ShoppingCart(cart.lineItems(), cart.user(), expectedTotal);

        // Create mutable map with cart wrapper
        // JEXL has issues with Java records, so we use a wrapper with getter methods
        // Using inline calculation instead of helper method due to JEXL limitations
        Map<String, Object> contextValues = new java.util.HashMap<>();
        CartWrapper cartWrapper = new CartWrapper(cart);
        contextValues.put("cart", cartWrapper);
        EvaluationContext context = EvaluationContext.from(contextValues);

        ExpressionEvaluationResult result = engine.evaluate(JEXL_CART_TOTAL_EXPRESSION, context);

        assertNotNull(result);
        assertTrue((Boolean) result.value());
    }

    @Test
    void testJexlInvalidCart_TotalMismatchesLineItems() throws ExpressionEvaluationException {
        ShoppingCart cart = createShoppingCart();
        BigDecimal expectedTotal = TestHelper.sumLineItemsTotal(cart.lineItems());
        BigDecimal wrongTotal = expectedTotal.add(new BigDecimal("10.00"));
        cart = new ShoppingCart(cart.lineItems(), cart.user(), wrongTotal);

        // Create mutable map with cart wrapper and helper object
        // JEXL has issues with Java records, so we use a wrapper with getter methods
        Map<String, Object> contextValues = new java.util.HashMap<>();
        contextValues.put("cart", new CartWrapper(cart));
        contextValues.put("helper", new TestHelper());
        EvaluationContext context = EvaluationContext.from(contextValues);

        ExpressionEvaluationResult result = engine.evaluate(JEXL_CART_TOTAL_EXPRESSION, context);

        assertNotNull(result);
        assertFalse((Boolean) result.value());
    }

    @Test
    void testJexlInvalidCart_TotalExceedsLimit() throws ExpressionEvaluationException {
        List<LineItem> lineItems = createShoppingCart().lineItems();
        User lowLimitUser = new User("login123", "user456", new BigDecimal("400"), UserStatus.ACTIVE, "user@example.com");
        BigDecimal expectedTotal = TestHelper.sumLineItemsTotal(lineItems);
        ShoppingCart cart = new ShoppingCart(lineItems, lowLimitUser, expectedTotal);

        // Create mutable map with cart wrapper and helper
        Map<String, Object> contextValues = new java.util.HashMap<>();
        contextValues.put("cart", new CartWrapper(cart));
        contextValues.put("helper", new TestHelper());
        EvaluationContext context = EvaluationContext.from(contextValues);

        ExpressionEvaluationResult result = engine.evaluate(JEXL_USER_LIMIT_EXPRESSION, context);

        assertNotNull(result);
        assertFalse((Boolean) result.value());
    }

    @Test
    void testJexlInvalidCart_TotalDoesNotExceedLimit() throws ExpressionEvaluationException {
        List<LineItem> lineItems = createShoppingCart().lineItems();
        User lowLimitUser = new User("login123", "user456", new BigDecimal("1100"), UserStatus.ACTIVE, "user@example.com");
        BigDecimal expectedTotal = TestHelper.sumLineItemsTotal(lineItems);
        ShoppingCart cart = new ShoppingCart(lineItems, lowLimitUser, expectedTotal);

        // Create mutable map with cart wrapper and helper
        Map<String, Object> contextValues = new java.util.HashMap<>();
        contextValues.put("cart", new CartWrapper(cart));
        contextValues.put("helper", new TestHelper());
        EvaluationContext context = EvaluationContext.from(contextValues);

        ExpressionEvaluationResult result = engine.evaluate(JEXL_USER_LIMIT_EXPRESSION, context);

        assertNotNull(result);
        assertTrue((Boolean) result.value());
    }

    @Test
    void testJexlInvalidCart_BothConditionsFail() throws ExpressionEvaluationException {
        List<LineItem> lineItems = createShoppingCart().lineItems();
        User lowLimitUser = new User("login123", "user456", new BigDecimal("400"), UserStatus.ACTIVE, "user@example.com");
        BigDecimal expectedTotal = TestHelper.sumLineItemsTotal(lineItems);
        BigDecimal wrongTotal = expectedTotal.add(new BigDecimal("10.00"));
        ShoppingCart cart = new ShoppingCart(lineItems, lowLimitUser, wrongTotal);

        // Create mutable map with cart wrapper and helper object
        // JEXL has issues with Java records, so we use a wrapper with getter methods
        Map<String, Object> contextValues = new java.util.HashMap<>();
        contextValues.put("cart", new CartWrapper(cart));
        contextValues.put("helper", new TestHelper());
        EvaluationContext context = EvaluationContext.from(contextValues);

        ExpressionEvaluationResult result = engine.evaluate(JEXL_CART_TOTAL_EXPRESSION, context);
        assertNotNull(result);
        assertFalse((Boolean) result.value());

        result = engine.evaluate(JEXL_USER_LIMIT_EXPRESSION, context);
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

