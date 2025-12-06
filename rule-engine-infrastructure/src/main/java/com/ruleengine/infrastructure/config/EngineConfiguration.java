package com.ruleengine.infrastructure.config;

import com.ruleengine.domain.factory.EngineType;
import com.ruleengine.domain.strategy.ExpressionEvaluationStrategy;
import com.ruleengine.domain.strategy.ScriptEvaluationStrategy;
import com.ruleengine.infrastructure.factory.EngineStrategyRegistry;
import com.ruleengine.infrastructure.strategy.expression.JexlExpressionEngine;
import com.ruleengine.infrastructure.strategy.expression.MvelExpressionEngine;
import com.ruleengine.infrastructure.strategy.expression.SpelExpressionEngine;
import com.ruleengine.infrastructure.strategy.script.GroovyScriptEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for wiring engine strategies.
 * Registers available strategies (SPEL, MVEL, JEXL, Groovy) with the engine strategy registry.
 * 
 * Module: rule-engine-infrastructure
 * Layer: Infrastructure
 */
@Configuration
public class EngineConfiguration {

    @Bean
    public ExpressionEvaluationStrategy spelExpressionEngine() {
        return new SpelExpressionEngine();
    }

    @Bean
    public ExpressionEvaluationStrategy mvelExpressionEngine() {
        return new MvelExpressionEngine();
    }

    @Bean
    public ExpressionEvaluationStrategy jexlExpressionEngine() {
        return new JexlExpressionEngine();
    }

    @Bean
    public ScriptEvaluationStrategy groovyScriptEngine() {
        return new GroovyScriptEngine();
    }

    @Bean
    public EngineStrategyRegistry engineStrategyRegistry() {
        EngineStrategyRegistry registry = new EngineStrategyRegistry();
        
        // Register expression engines by calling bean methods
        registry.registerExpressionStrategy(EngineType.SPEL, spelExpressionEngine());
        registry.registerExpressionStrategy(EngineType.MVEL, mvelExpressionEngine());
        registry.registerExpressionStrategy(EngineType.JEXL, jexlExpressionEngine());
        
        // Register script engines
        registry.registerScriptStrategy(EngineType.GROOVY, groovyScriptEngine());
        
        return registry;
    }
}

