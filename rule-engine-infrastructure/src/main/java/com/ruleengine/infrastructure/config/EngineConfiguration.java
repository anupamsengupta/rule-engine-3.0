package com.ruleengine.infrastructure.config;

import com.ruleengine.domain.factory.EngineType;
import com.ruleengine.domain.strategy.ExpressionEvaluationStrategy;
import com.ruleengine.domain.strategy.ScriptEvaluationStrategy;
import com.ruleengine.infrastructure.factory.EngineStrategyRegistry;
import com.ruleengine.infrastructure.strategy.expression.SpelExpressionEngine;
import com.ruleengine.infrastructure.strategy.script.GroovyScriptEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for wiring engine strategies.
 * Registers available strategies (SPEL, Groovy) with the engine strategy registry.
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
    public ScriptEvaluationStrategy groovyScriptEngine() {
        return new GroovyScriptEngine();
    }

    @Bean
    public EngineStrategyRegistry engineStrategyRegistry(
            ExpressionEvaluationStrategy spelExpressionEngine,
            ScriptEvaluationStrategy groovyScriptEngine
    ) {
        EngineStrategyRegistry registry = new EngineStrategyRegistry();
        
        // Register SPEL expression engine
        registry.registerExpressionStrategy(EngineType.SPEL, spelExpressionEngine);
        
        // Register Groovy script engine
        registry.registerScriptStrategy(EngineType.GROOVY, groovyScriptEngine);
        
        return registry;
    }
}

