package com.ruleengine.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * Spring Boot application entry point for the Rule Engine module.
 * 
 * Module: rule-engine-app
 * Layer: App
 */
@SpringBootApplication(scanBasePackages = "com.ruleengine")
@EntityScan(basePackages = "com.ruleengine.persistence.entity")
public class RuleEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(RuleEngineApplication.class, args);
    }
}

