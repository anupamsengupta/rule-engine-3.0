package com.ruleengine.domain.operator;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enumeration of comparison operators used in rules and expressions.
 * Provides bidirectional mapping between enum values and their symbol representations.
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public enum ComparisonOperator {
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),
    EQ("=="),
    NE("!=");

    private final String symbol;

    ComparisonOperator(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Returns the symbol representation of this operator.
     */
    public String getSymbol() {
        return symbol;
    }

    private static final Map<String, ComparisonOperator> SYMBOL_TO_OPERATOR = Stream.of(values())
            .collect(Collectors.toMap(ComparisonOperator::getSymbol, Function.identity()));

    /**
     * Finds a ComparisonOperator by its symbol.
     *
     * @param symbol The symbol string (e.g., ">", "==")
     * @return Optional containing the operator if found
     */
    public static Optional<ComparisonOperator> fromSymbol(String symbol) {
        return Optional.ofNullable(SYMBOL_TO_OPERATOR.get(symbol));
    }
}

