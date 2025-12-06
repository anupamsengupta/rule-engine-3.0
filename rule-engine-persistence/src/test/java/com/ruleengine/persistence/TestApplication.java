package com.ruleengine.persistence;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Minimal Spring Boot application class for testing purposes only.
 * This allows @DataJpaTest and other Spring Boot test annotations to work.
 * 
 * Module: rule-engine-persistence
 * Layer: Persistence (Test only)
 */
@SpringBootApplication
@EntityScan(basePackages = "com.ruleengine.persistence.entity")
@EnableJpaRepositories(basePackages = "com.ruleengine.persistence.repository")
public class TestApplication {
    // Empty - used only for test context
}

