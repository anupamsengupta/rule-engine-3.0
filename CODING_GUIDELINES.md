# Coding Guidelines (Template for All Projects)

These guidelines ensure predictable, maintainable, scalable, and testable code across all projects.  
All developers and AI-assisted code generation tools MUST comply with these rules.

---

## 1. Language, Framework & Maven Standards

- **Java Version:** Java 21+
- **Build Tool:** Apache Maven (multi-module)
- **Framework:** Spring Boot 3.x (application/infrastructure only)
- **Testing:** JUnit 5 (JUnit Jupiter)
- **Coverage:** JaCoCo integrated via `jacoco-maven-plugin` in the parent POM
- **Test Execution:** `maven-surefire-plugin`

All modules MUST inherit Java version and plugin configurations from the parent POM.

---

## 2. SOLID, Maintainability & Complexity Rules

### 2.1 SOLID Principles
- **S**ingle Responsibility — small, cohesive classes
- **O**pen/Closed — add new strategies/commands instead of modifying existing ones
- **L**iskov Substitution — all strategy implementations must behave consistently
- **I**nterface Segregation — small, purpose-focused interfaces
- **D**ependency Inversion — domain defines abstractions; infrastructure implements them

### 2.2 Cyclomatic Complexity
- Recommended maximum **≤ 10** per method.
- Reduce complexity using:
  - Strategy pattern
  - Command pattern
  - Enum-based factories
  - Value objects
  - Extract methods when needed

---

## 3. Design Patterns (MANDATORY WHEN APPLICABLE)

### 3.1 Strategy Pattern
Use for pluggable or replaceable behaviors:
- Expression engines (MVEL, SPEL, JEXL)
- Script evaluation engines (Groovy)
- Rule evaluation strategies
- Computational variations

Rules:
- Implement shared interfaces defined in domain.
- Prefer stateless or immutable strategies.
- Strategy selection done via:
  - Registry, or
  - Enum-based factory.
### 3.2 Command Pattern
Use for:
- Individual business operations
- Workflow steps
- Side-effect operations (e.g., message publishing)

Rules:
- Each command encapsulates one business action.
- Commands must be stateless or immutable.
- Commands return result objects (records).

### Enum-Based Factories

- Use enums to map types → implementations.
- Avoid switch cases in business logic.
- Allow dynamic registration for pluggability.

---

## 4. Coding Style & Structure

- Use Java 21 features:
  - Records
  - Pattern matching
  - Enhanced switch
- Prefer immutability.
- Avoid Lombok.
- Use constructor injection only.
- No field injection.
- Name classes clearly and consistently:
  - `*Controller`, `*Service`, `*UseCase`, `*Strategy`, `*Command`, `*Factory`, `*Repository`

---

## 5. Spring Guidelines

- Controllers:
  - DTO validation
  - Delegate to application services
  - No business logic
- Services:
  - Use-case orchestration
- Repositories:
  - Persistence only
  - must not contain domain logic.
- Configuration:
  - Bean wiring and factories in `infrastructure` module
- Avoid circular dependencies

---

## 6. Persistence Guidelines

- Entities MUST reside only in `project-infrastructure`.
- Domain MUST use domain models, not JPA entities.
- Mapping:
  - Use manual or tool-based mappers
  - NEVER return JPA entity to API consumers

---

## 7. Error Handling Standards

- Domain exceptions:
  - Business errors
- Application exceptions:
  - Use-case flow issues
- API exceptions:
  - Translate to proper HTTP responses
- Wrap external exceptions inside domain/application exceptions.

---

## 8. Logging & Observability

- Use SLF4J for logging.
- Domain layer MUST NOT log.
- Infrastructure logs:
  - INFO for business transitions
  - DEBUG for diagnostics
  - WARN/ERROR for failures
- Avoid logging sensitive data.

---

## 9. Testing Guidelines

- Use JUnit 5 for all unit tests.
- Domain layer MUST be fully testable without Spring context.
- Integration tests for repositories, external APIs, and app wiring.
- Use AssertJ or equivalent fluent assertions.
- JaCoCo MUST run during the build with the `check` goal enforcing coverage.

---

## 10. AI Code Generation Rules

When using Cursor, Junie, or any AI system:

1. ALWAYS read and follow:
   - `ARCHITECTURE.md`
   - `CODING_GUIDELINES.md`
2. Identify the appropriate module before writing code.
3. Explain briefly:
   - Why the design was chosen,
   - How it fits the architecture.
4. ALWAYS specify:
   - Which Maven module the generated file belongs in.
   - Which architectural layer it belongs to (domain/application/api/infrastructure/app).
5. Avoid introducing:
   - New patterns,
   - New frameworks,
   - Unnecessary abstractions.
6. Maintain consistent naming, layering, and patterns.
7. MUST use:
   - Java 21
   - Multi-module Maven layout
   - JUnit 5 test structures
8. MUST use patterns:
   - Strategy
   - Command
   - Enum-based factories
9. MUST warn and propose alternatives if the user request violates the architecture or coding rules.
