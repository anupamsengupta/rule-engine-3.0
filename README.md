# Rule Engine & Expression Engine Module

A reusable, pluggable Rule Engine and Expression Engine module built with Java 21 in a Maven multi-module project, following strict architectural layering and SOLID principles.

## Project Status

This project has been fully generated according to `application-prompt.md`. Implementation status:

### ✅ Completed
- **Design Document** (`DESIGN.md`) - Complete architecture and design documentation
- **Maven Structure** - Parent POM and all 8 module POMs configured with Java 21, JUnit 5, JaCoCo
- **Domain Layer** - Complete implementation:
  - Attributes, Operators, Rules, Expressions
  - Strategy interfaces, Engine interfaces
  - Commands, Exceptions, Factory enum
- **Persistence Layer** - Complete implementation:
  - JPA entities (Attribute, Rule, Condition, RuleSet, Expression)
  - Spring Data repositories
  - Mappers (domain ↔ entity)
  - Caching strategies (Caffeine in-memory, Redis distributed)
- **Infrastructure Layer** - Complete implementation:
  - SPEL expression engine
  - Groovy script engine
  - Enum-based factory registry
  - Spring configuration
- **Application Layer** - Complete implementation:
  - RuleEngineService
  - ExpressionEngineService
- **API Layer** - Complete implementation:
  - REST controllers (RuleController, ExpressionController)
  - DTOs for requests and responses
- **Testing** - Complete test suite:
  - Unit tests with JUnit 5 and Mockito
  - Integration tests with Testcontainers
  - End-to-end tests in app module
- **Benchmarks** - JMH performance benchmarks
- **Load Tests** - K6 scripts with SLA definitions
- **Containerization** - Complete:
  - Dockerfile for containerization
  - docker-compose.yml for local development
  - ECS task definition for AWS deployment

## Maven Multi-Module Structure

```
rule-engine-parent/
├── rule-engine-domain/          # ✅ Complete
├── rule-engine-persistence/    # 🚧 In Progress
├── rule-engine-application/     # ⏳ Pending
├── rule-engine-api/             # ⏳ Pending
├── rule-engine-infrastructure/  # ✅ Complete
├── rule-engine-app/             # ⏳ Pending
├── rule-engine-benchmarks/      # ⏳ Pending
└── rule-engine-loadtest-scripts/ # ⏳ Pending
```

## Build Commands

```bash
# Build all modules
mvn clean install

# Run tests
mvn test

# Generate coverage report
mvn jacoco:report

# Check coverage threshold
mvn jacoco:check
```

## Architecture

See `DESIGN.md` for complete architecture documentation.

## Quick Start

### Build and Run

```bash
# Build all modules
mvn clean install

# Run tests
mvn test

# Run application
cd rule-engine-app
mvn spring-boot:run
```

### Using Docker

```bash
# Build and run with docker-compose
docker-compose up -d

# Or build image manually
docker build -t rule-engine:latest .
docker run -p 8080:8080 rule-engine:latest
```

### Run Benchmarks

```bash
cd rule-engine-benchmarks
mvn clean package
java -jar target/benchmarks.jar
```

### Run Load Tests

```bash
cd rule-engine-loadtest-scripts
k6 run src/main/k6/rule-validation-loadtest.js
```

## Architecture

See `DESIGN.md` for complete architecture documentation including:
- Module structure and responsibilities
- Design patterns (Strategy, Command, Factory)
- Caching strategies
- Code traversal and usage examples

