package com.ruleengine.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot application entry point for the Rule Engine module.
 * 
 * Module: rule-engine-app
 * Layer: App
 */
@SpringBootApplication(scanBasePackages = "com.ruleengine")
public class RuleEngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(RuleEngineApplication.class, args);
    }
}

