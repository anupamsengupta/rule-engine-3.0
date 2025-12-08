package com.ruleengine.application.config;

import com.ruleengine.application.service.ConditionService;
import com.ruleengine.application.service.ExpressionEngineService;
import com.ruleengine.application.service.RuleEngineService;
import com.ruleengine.domain.factory.EngineType;
import com.ruleengine.infrastructure.factory.EngineStrategyRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for application layer services.
 * 
 * Module: rule-engine-application
 * Layer: Application
 */
@Configuration
public class ApplicationConfiguration {

    @Bean
    public RuleEngineService ruleEngineService(
            EngineStrategyRegistry engineStrategyRegistry,
            ConditionService conditionService
    ) {
        return new RuleEngineService(engineStrategyRegistry, EngineType.SPEL, conditionService);
    }

    @Bean
    public ExpressionEngineService expressionEngineService(
            EngineStrategyRegistry engineStrategyRegistry
    ) {
        return new ExpressionEngineService(engineStrategyRegistry, EngineType.SPEL);
    }
}

