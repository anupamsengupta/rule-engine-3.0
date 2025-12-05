# Rule Engine & Expression Engine - Design Document

## Overview

This document outlines the design for a reusable Rule Engine and Expression Engine module following strict layering, SOLID principles, and pattern-based architecture. The system is built as a Maven multi-module project targeting Java 21.

---

## Module Structure

```
rule-engine-parent/
├── rule-engine-domain/          # Pure domain models, interfaces, no dependencies
├── rule-engine-persistence/    # JPA entities, repositories, mappers, caching
├── rule-engine-application/    # Use case orchestration
├── rule-engine-api/            # REST controllers, DTOs
├── rule-engine-infrastructure/ # Expression engine implementations (SPEL, Groovy)
├── rule-engine-app/            # Spring Boot application + E2E tests
├── rule-engine-benchmarks/     # JMH benchmarks
└── rule-engine-loadtest-scripts/ # K6 load test scripts
```

---

## Domain Layer (`rule-engine-domain`)

### Package Structure

```
com.ruleengine.domain
├── attribute/
│   ├── Attribute.java              # Domain model for attributes
│   └── AttributeType.java          # Enum: STRING, NUMBER, BOOLEAN, DATE, etc.
├── operator/
│   ├── ComparisonOperator.java     # Enum: >, >=, <, <=, ==, !=
│   ├── LogicalOperator.java        # Enum: AND, OR, NOT
│   └── ParenthesisToken.java       # Enum: OPEN, CLOSE
├── rule/
│   ├── Rule.java                   # Rule domain model
│   ├── RuleValidationResult.java   # Result record
│   ├── RuleSet.java                # Collection of rules
│   ├── Condition.java              # Single condition
│   └── RuleMetadata.java           # Rule metadata
├── expression/
│   ├── Expression.java             # Expression domain model
│   └── ExpressionEvaluationResult.java  # Result record
├── context/
│   └── EvaluationContext.java     # Map-based context for attribute values
├── strategy/
│   ├── ExpressionEvaluationStrategy.java  # Interface for expression engines
│   └── ScriptEvaluationStrategy.java      # Interface for scripting engines
├── engine/
│   ├── RuleEngine.java            # Interface for rule validation
│   └── ExpressionEngine.java      # Interface for expression evaluation
├── command/
│   ├── ValidateRuleCommand.java   # Command for rule validation
│   └── EvaluateExpressionCommand.java  # Command for expression evaluation
├── factory/
│   └── EngineType.java            # Enum-based factory: MVEL, SPEL, JEXL, GROOVY
└── exception/
    ├── RuleEvaluationException.java
    └── ExpressionEvaluationException.java
```

### Key Design Decisions

1. **Attributes Model**: Immutable records with code, type, description, and constraints
2. **Operators Model**: Enums with symbol mapping for bidirectional conversion
3. **Rule Model**: Immutable with conditions list, delegates validation to strategies
4. **Expression Model**: Immutable with expression string, delegates evaluation to strategies
5. **Strategy Interfaces**: Separate interfaces for expression languages vs scripting
6. **Command Pattern**: Encapsulates validation/evaluation operations
7. **Enum-Based Factory**: `EngineType` enum for strategy selection

---

## Persistence Layer (`rule-engine-persistence`)

### Package Structure

```
com.ruleengine.persistence
├── entity/
│   ├── AttributeEntity.java
│   ├── RuleEntity.java
│   ├── ConditionEntity.java
│   ├── RuleSetEntity.java
│   └── ExpressionEntity.java
├── repository/
│   ├── AttributeRepository.java
│   ├── RuleRepository.java
│   ├── RuleSetRepository.java
│   └── ExpressionRepository.java
├── mapper/
│   ├── AttributeMapper.java
│   ├── RuleMapper.java
│   ├── ConditionMapper.java
│   ├── RuleSetMapper.java
│   └── ExpressionMapper.java
├── cache/
│   ├── CacheStrategy.java          # Interface for cache strategies
│   ├── CaffeineCacheStrategy.java  # In-memory cache
│   ├── RedisCacheStrategy.java     # Distributed cache
│   └── CacheConfiguration.java     # Cache configuration
└── config/
    └── PersistenceConfiguration.java
```

### Key Design Decisions

1. **Separate Persistence Module**: Follows architecture requirement for separate persistence layer
2. **Mappers**: Manual mappers to convert between domain models and entities
3. **Caching Strategy**: Interface-based approach supporting Caffeine (in-memory) and Redis (distributed)
4. **Repository Pattern**: Spring Data JPA repositories for persistence operations

---

## Infrastructure Layer (`rule-engine-infrastructure`)

### Package Structure

```
com.ruleengine.infrastructure
├── strategy/
│   ├── expression/
│   │   └── SpelExpressionEngine.java      # SPEL implementation
│   └── script/
│       └── GroovyScriptEngine.java        # Groovy implementation
├── factory/
│   └── EngineStrategyRegistry.java        # Registry mapping EngineType → Strategy
└── config/
    └── EngineConfiguration.java           # Spring configuration for wiring
```

### Key Design Decisions

1. **Strategy Implementations**: SPEL and Groovy as examples
2. **Enum-Based Factory**: Registry pattern for engine selection
3. **Exception Wrapping**: Infrastructure exceptions wrapped in domain exceptions

---

## Application Layer (`rule-engine-application`)

### Package Structure

```
com.ruleengine.application
├── service/
│   ├── RuleEngineService.java      # Orchestrates rule validation
│   └── ExpressionEngineService.java  # Orchestrates expression evaluation
└── mapper/
    └── DtoMapper.java              # Maps DTOs to domain models
```

---

## API Layer (`rule-engine-api`)

### Package Structure

```
com.ruleengine.api
├── controller/
│   ├── RuleController.java
│   └── ExpressionController.java
└── dto/
    ├── RuleValidationRequest.java
    ├── RuleValidationResponse.java
    ├── ExpressionEvaluationRequest.java
    └── ExpressionEvaluationResponse.java
```

---

## Testing Strategy

### Unit Tests
- **Domain**: Pure unit tests without Spring context
- **Application**: Unit tests with mocked dependencies
- **Infrastructure**: Unit tests for strategies
- **API**: Unit tests for controllers with mocked services

### Integration Tests
- **Persistence**: DBUnit or Testcontainers for database integration
- **Infrastructure**: Integration tests for expression engines

### End-to-End Tests
- **App Module**: Full application tests with embedded database
- **API Tests**: REST API integration tests

### Benchmarks
- **JMH**: Performance benchmarks for critical paths

### Load Tests
- **K6**: Load test scripts with SLA definitions

---

## Design Patterns Applied

1. **Strategy Pattern**: 
   - Expression evaluation engines (SPEL, Groovy)
   - Cache strategies (Caffeine, Redis)

2. **Command Pattern**:
   - `ValidateRuleCommand`, `EvaluateExpressionCommand`

3. **Enum-Based Factory**:
   - `EngineType` enum + `EngineStrategyRegistry`

4. **Repository Pattern**:
   - Spring Data JPA repositories

5. **Mapper Pattern**:
   - Domain ↔ Entity conversion
   - DTO ↔ Domain conversion

---

## Caching Strategy

Two-tier caching approach:
1. **In-Memory (Caffeine)**: Fast local cache for frequently accessed rules/expressions
2. **Distributed (Redis)**: Shared cache across instances for consistency

Cache strategy is pluggable via `CacheStrategy` interface.

---

## Containerization

- Dockerfile for building application image
- AWS ECS/EKS ready configuration
- Health check endpoints
- Environment-based configuration

---

## Code Traversal & Usage

### Creating and Validating a Rule

```java
// 1. Create attributes
Attribute ageAttr = new Attribute("customer.age", AttributeType.NUMBER, "Customer age");

// 2. Create condition
Condition condition = new Condition(ageAttr, ComparisonOperator.GTE, 18);

// 3. Create rule
Rule rule = new Rule("rule-1", "Adult customer", List.of(condition), RuleMetadata.defaults());

// 4. Create evaluation context
EvaluationContext context = EvaluationContext.from(Map.of("customer.age", 25));

// 5. Validate using service
RuleEngineService service = ...; // Injected
RuleValidationResult result = service.validateRule(rule, context);
```

### Evaluating an Expression

```java
// 1. Create expression
Expression expression = new Expression("customer.age >= 18");

// 2. Create evaluation context
EvaluationContext context = EvaluationContext.from(Map.of("customer.age", 25));

// 3. Evaluate using service
ExpressionEngineService service = ...; // Injected
ExpressionEvaluationResult result = service.evaluateExpression(expression, context);
```

### Persisting Rules

```java
// 1. Save rule via repository
RuleRepository repository = ...; // Injected
RuleEntity entity = RuleMapper.toEntity(rule);
repository.save(entity);

// 2. Retrieve rule
Optional<RuleEntity> entityOpt = repository.findById("rule-1");
Rule rule = RuleMapper.toDomain(entityOpt.get());
```

---

## Build & Run

### Build Commands

```bash
# Build all modules
mvn clean install

# Run tests
mvn test

# Generate coverage report
mvn jacoco:report

# Check coverage threshold
mvn jacoco:check

# Run benchmarks
cd rule-engine-benchmarks
mvn clean install
java -jar target/benchmarks.jar

# Run load tests
cd rule-engine-loadtest-scripts
k6 run rule-validation-loadtest.js
```

### Running the Application

```bash
# Run Spring Boot app
cd rule-engine-app
mvn spring-boot:run

# Or with Docker
docker build -t rule-engine:latest .
docker run -p 8080:8080 rule-engine:latest
```

---

## Compliance Notes

✅ **Layering**: Domain has no Spring/JPA dependencies  
✅ **SOLID**: Single responsibility, interfaces for strategies, dependency inversion  
✅ **Low Complexity**: Strategy pattern avoids conditionals, commands encapsulate operations  
✅ **Java 21**: Uses records, no Lombok  
✅ **Patterns**: Strategy, Command, Enum Factory, Repository, Mapper  
✅ **Testing**: JUnit 5, Mockito, DBUnit, Testcontainers  
✅ **Performance**: JMH benchmarks, K6 load tests  
✅ **Containerization**: Docker + AWS ECS/EKS ready

