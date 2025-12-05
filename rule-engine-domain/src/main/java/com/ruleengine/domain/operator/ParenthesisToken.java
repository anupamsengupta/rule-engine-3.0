package com.ruleengine.domain.operator;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enumeration of parenthesis tokens for grouping expressions.
 * Provides bidirectional mapping between enum values and their symbol representations.
 * 
 * Module: rule-engine-domain
 * Layer: Domain
 */
public enum ParenthesisToken {
    OPEN("("),
    CLOSE(")");

    private final String symbol;

    ParenthesisToken(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Returns the symbol representation of this token.
     */
    public String getSymbol() {
        return symbol;
    }

    private static final Map<String, ParenthesisToken> SYMBOL_TO_TOKEN = Stream.of(values())
            .collect(Collectors.toMap(ParenthesisToken::getSymbol, Function.identity()));

    /**
     * Finds a ParenthesisToken by its symbol.
     *
     * @param symbol The symbol string ("(" or ")")
     * @return Optional containing the token if found
     */
    public static Optional<ParenthesisToken> fromSymbol(String symbol) {
        return Optional.ofNullable(SYMBOL_TO_TOKEN.get(symbol));
    }
}

