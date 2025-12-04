# Coding Guidelines (Template for All Projects)

These guidelines ensure uniform, predictable, maintainable, and testable code across all projects.

---

## 1. Language & Framework Standards

- Java 21+
- Spring Boot 3.x for application bootstrapping
- No Lombok (use records, pattern matching, sealed classes)
- Prefer functional style where it improves clarity
- Use constructor injection only
- No field injection

---

## 2. SOLID, Maintainability & Complexity Rules

1. **S – Single Responsibility**
   - Each class does *one thing*.
   - Split classes >300 lines.

2. **O – Open/Closed**
   - Prefer adding new strategies/commands over modifying existing logic.

3. **L – Liskov Substitution**
   - Ensure all implementations of an interface behave consistently.

4. **I – Interface Segregation**
   - Keep interfaces narrow (one purpose).

5. **D – Dependency Inversion**
   - Upper layers depend only on domain interfaces.

### **Cyclomatic Complexity**
- Max threshold: **10**
- Use:
  - Strategy pattern
  - Command pattern
  - Enum factories
  - Polymorphism  
  instead of nested conditionals.

---

## 3. Design Pattern Guidelines

### Strategy Pattern

Use for:
- Evaluation
- Calculation
- Conditional workflows
- Interchangeable business rules

Rules:
- Strategies implement a shared interface.
- Strategy selection done via:
  - Registry, or
  - Enum-based factory.

### Command Pattern

Use for:
- Business actions
- External side effects
- Workflow steps

Rules:
- Each command encapsulates one business action.
- Commands must be stateless or immutable.
- Commands return result objects (records).

### Enum-Based Factories

- Use enums to map types → implementations.
- Avoid switch cases in business logic.
- Allow dynamic registration for pluggability.

---

## 4. Code Style Standards

- Use Java records for DTOs and simple value objects.
- Use immutable objects where possible.
- Avoid static util classes unless purely functional.
- Prefer `Optional` for absence, not for control flow.
- Use meaningful names; avoid abbreviations.
- Keep classes small and cohesive.

---

## 5. Spring-Specific Guidelines

- Controllers **must not contain business logic**.
- Services orchestrate; do not mix persistence logic.
- Repositories must not contain domain logic.
- Avoid circular dependencies.
- Use Spring configuration classes for bean wiring.
- Avoid `@Transactional` at the controller level.

---

## 6. Persistence Guidelines

- Entities in infrastructure layer only.
- Domain layer uses domain models, not JPA entities.
- Use mappers (manual or MapStruct) between domain ↔ entity.
- Use value objects for domain-specific constraints.

---

## 7. Error Handling Guidelines

- Domain errors → domain exceptions.
- Infrastructure errors → wrapped exceptions.
- API layer must convert exceptions to standardized API error responses.

---

## 8. Logging Guidelines

- Use SLF4J / Logback conventions.
- No logging in domain models.
- Avoid logging sensitive information.
- Log at:
  - DEBUG for diagnostics
  - INFO for state transitions
  - WARN/ERROR for application failures

---

## 9. Tests & Documentation

- Every non-trivial class needs a unit test.
- Domain logic must be testable without Spring context.
- Use JUnit 5 + AssertJ + Testcontainers (if needed).
- Add JavaDoc on:
  - Public interfaces
  - Strategies
  - Commands
  - Factories
  - Domain models

---

## 10. AI Code Generation Rules (Cursor / Junie)

When generating code, the AI must:

1. Follow this document and `ARCHITECTURE.md` strictly.
2. Identify the appropriate module before writing code.
3. Explain briefly:
   - Why the design was chosen,
   - How it fits the architecture.
4. Avoid introducing:
   - New patterns,
   - New frameworks,
   - Unnecessary abstractions.
5. Maintain consistent naming, layering, and patterns.

If a user prompt violates architectural rules, AI must warn and propose alternative solutions.
