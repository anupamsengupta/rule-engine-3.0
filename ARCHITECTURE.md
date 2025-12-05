# Project Architecture Specification (Template)

This document defines the standard architecture, layering rules, design constraints, 
and module responsibilities for any new project created using this template. 
It also defines the standard Maven multi-module structure and build configuration.

It serves as the authoritative reference for all developers and all AI-assisted code generation tools.

---

## 1. Build & Platform Standards

### 1.1 Build Tool
- Use **Apache Maven** as the build tool.
- The project MUST be a **Maven multi-module project**.
- Use the latest stable Maven version.

### 1.2 Java Version
- The project MUST target **Java 21**.
- Configure Java 21 centrally in the parent `pom.xml` using `maven-compiler-plugin`:
  - `source = 21`
  - `target = 21`
  - `release = 21` (preferred)

### 1.3 Testing & Coverage
- Use **JUnit 5 (JUnit Jupiter)** for all tests.
- Use **JaCoCo** (`jacoco-maven-plugin`) for test coverage.
- Build must include:
  - Unit tests via `maven-surefire-plugin`
  - Code coverage reporting and enforcement via JaCoCo `check` goal

---

## 2. Maven Multi-Module Structure

A typical project MUST follow this structure:

parent-pom/
├── project-domain
├── project-persistence
├── project-application
├── project-api
├── project-infrastructure
└── project-app
└── project-benchmarks
└── project-loadtest-scripts

you can replace project with rule-engine

### 2.1 Root `pom.xml` Responsibilities
- Packaging: `pom`
- Defines:
  - `<modules>`
  - Common properties (`java.version=21`)
  - Dependency versions & BOMs
  - Plugin management for:
    - `maven-compiler-plugin`
    - `maven-surefire-plugin`
    - `jacoco-maven-plugin`
- Enforces global build rules (e.g., coverage thresholds)

---

## 3. Architectural Principles

### 3.1 Layered Architecture
- **API Layer**  
  REST endpoints, DTOs, request/response mappers
- **Application Layer**  
  Use cases, orchestrations, business workflows
- **Domain Layer**  
  value objects, domain services, strategies, commands
- **Persistence Layer**  
  Entities, Spring repositories, caching for write through
- **Infrastructure Layer**  
  external API integrations, configuration
- **App Layer**  
  Spring Boot application starter

### 3.2 Dependency Direction

api → application → domain
↓
infrastructure
↓
app


- Domain MUST NOT depend on Spring, JPA, or external libraries.
- Infrastructure implements domain interfaces.

### 3.3 Key Principles
**SOLID Principles**
- **S**ingle Responsibility: Each class handles one reason to change.
- **O**pen/Closed: Extend via new implementations, avoid modifying existing classes.
- **L**iskov Substitution: Strategies, commands, and factories must rely on interfaces.
- **I**nterface Segregation: Small, focused interfaces.
- **D**ependency Inversion: Upper layers depend on abstractions; infrastructure implements them.
- **Low Cyclomatic Complexity**
- Avoid deeply nested branching.
- Use polymorphism, strategy pattern, command pattern, and value objects instead of conditionals.
- Extract sub-methods when complexity > 10.
---

## 4. Domain Design Rules

### 4.1 Domain Models
- Immutable where possible.
- Use Java 21 records for simple value objects.
- Business logic resides here or in the application layer.

### 4.2 Strategy Pattern Usage
Use when:
- A computation/evaluation has multiple interchangeable implementations.
- Behavior varies based on configuration, type, or input context.

Example:
- Expression evaluators (MVEL, JEXL, SPEL)
- Pricing engines
- Notification mechanisms
- Validation strategies
### 4.3 Command Pattern Usage
Use for:
- Business actions (createUser, applyDiscount, scheduleTrade, etc.).
- External side-effects (publishEvent, sendEmail).
- Workflow steps.

### 4.4 Enum-Based Factories
- For selecting strategies/commands based on type enums.
- Replace switch statements with polymorphic lookup tables.

---

## 5. Application Layer Rules
- No domain logic should be written here.
- This layer:
- Coordinates domain strategies/commands.
- Maps DTOs ↔ domain objects.
- Manages transactional boundaries (if needed).
- Implements use-case classes (`*Service`, `*UseCase`, `*Handler`).

---

## 6. API Layer Rules
- REST controllers must be **thin**:
- Validate input.
- Invoke Application Services only.
- Map request → domain → response.
- Must not access repositories, domain entities, or infrastructure components directly.
- Use Java 21 records for all DTOs.

---

## 7. Infrastructure Layer Rules
- Contains:
  - JPA entities and repositories
  - Messaging integrations
  - External HTTP clients
  - Expression engine implementations (MVEL, SPEL, JEXL, Groovy)
  - Spring configuration
- Implements domain interfaces.
- Must not contain business logic.

---

## 8. App Layer Rules
- `*-app` module:
- Contains the Spring Boot application class.
- Only module with Spring Boot dependency.
- All bean configuration (factories, conditional beans, etc.) lives in infrastructure module.

---

## 9. Error Handling & Exceptions

- Use domain-specific exceptions in `domain`.
- Wrap external exceptions (DB, HTTP, expression engines) inside application-level exceptions.
- Never leak infrastructure-specific exceptions outside the infrastructure layer.

---

## 8. Logging & Observability

- Domain layer must remain logging-minimal or avoid logging.
- Application and infrastructure layers handle logging.
- Use structured logs.
- Add trace identifiers at the boundary (API layer).

---

## 10. Testing Architecture

1. **Unit tests**
- Domain logic tested without Spring.
- Strategies/commands have dedicated test suites.

2. **Integration tests**
- Repositories, cache, external systems.

3. **Contract tests**
- For external API integrations.

4. **End-to-end tests**
- Via main application module.

5. **Test Coverage**
- JaCoCo MUST be active with report + check goals.

---

## 11. AI-Assisted Code Generation Rules
- All code generation MUST comply with:
  - This `ARCHITECTURE.md`
  - `CODING_GUIDELINES.md`
- AI-generated code must:
- State which module it belongs to.
- State how it fits the architecture.
- Warn if the prompt violates architectural constraints.
- Do not introduce additional design patterns unless justified.
- AI tooling MUST:
  - Declare which Maven module each file belongs to
  - Follow the layering rules
  - Use Java 21 features
  - Assume JUnit 5 + JaCoCo setup
- If a request violates architecture:
  - The generator MUST warn and provide an alternative compliant solution.

