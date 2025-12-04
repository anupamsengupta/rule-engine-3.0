# Project Architecture Specification (Template)

This document defines the standard architecture, layering rules, design constraints, 
and module responsibilities for any new project created using this template. 
It serves as the authoritative reference for all code generation tools 
(AI-assisted or otherwise) and must be followed strictly.

---

## 1. Architectural Principles

1. **Layered Architecture**
   - Presentation / API Layer: Controllers, REST endpoints, GraphQL, WebSockets.
   - Application Layer: Orchestrates use cases, coordinates business logic.
   - Domain Layer: Pure domain models, business rules, interfaces (ports).
   - Infrastructure Layer: Implementations of persistence, external integrations, caching, messaging, etc.

2. **Multi-Module Project Structure**
   - `*-api`  
     Contains DTOs, REST controllers, request/response models.
   - `*-application`  
     Contains orchestrators, services, use-case coordinators.
   - `*-domain`  
     Contains domain models, interfaces, invariants, strategies, commands.
   - `*-infrastructure`  
     Contains persistence, external service clients, caching, configuration, 
     gateway implementations.
   - `*-app`  
     Runtime module containing the Spring Boot main application.

3. **Enforcement of Dependencies (Strict Direction)**

api → application → domain
↓
infrastructure

- Domain must **not** depend on Spring, JPA, HTTP, or any external library unless absolutely required.
- Infrastructure implements domain interfaces.

4. **SOLID Principles**
- **S**ingle Responsibility: Each class handles one reason to change.
- **O**pen/Closed: Extend via new implementations, avoid modifying existing classes.
- **L**iskov Substitution: Strategies, commands, and factories must rely on interfaces.
- **I**nterface Segregation: Small, focused interfaces.
- **D**ependency Inversion: Upper layers depend on abstractions; infrastructure implements them.

5. **Low Cyclomatic Complexity**
- Avoid deeply nested branching.
- Use polymorphism, strategy pattern, command pattern, and value objects instead of conditionals.
- Extract sub-methods when complexity > 10.

---

## 2. Domain Design Rules

1. **Domain Models**
- Immutable (`final` fields) wherever possible.
- Use records for simple value objects.
- Business rules belong here, not in controllers or repositories.

2. **Strategy Pattern Usage**
Use when:
- A computation/evaluation has multiple interchangeable implementations.
- Behavior varies based on configuration, type, or input context.

Example:
- Expression evaluators (MVEL, JEXL, SPEL)
- Pricing engines
- Notification mechanisms
- Validation strategies

3. **Command Pattern Usage**
Use for:
- Business actions (createUser, applyDiscount, scheduleTrade, etc.).
- External side-effects (publishEvent, sendEmail).
- Workflow steps.

4. **Enum-Based Factories**
- For selecting strategies/commands based on type enums.
- Replace switch statements with polymorphic lookup tables.

---

## 3. Application Layer Rules

- No domain logic should be written here.
- This layer:
- Coordinates domain strategies/commands.
- Maps DTOs ↔ domain objects.
- Manages transactional boundaries (if needed).
- Implements use-case classes (`*Service`, `*UseCase`, `*Handler`).

---

## 4. API Layer Rules

- REST controllers must be **thin**:
- Validate input.
- Invoke Application Services only.
- Map request → domain → response.
- Must not access repositories, domain entities, or infrastructure components directly.
- Use Java 21 records for all DTOs.

---

## 5. Infrastructure Layer Rules

- Contains:
- JPA repositories and entity mappings.
- Cache implementations.
- External API clients.
- Messaging (Kafka, SNS/SQS, etc.)
- Spring configuration.
- No business logic.
- Must implement domain interfaces.

---

## 6. Configuration & Bootstrapping Rules

- `*-app` module:
- Contains the Spring Boot application class.
- Only module with Spring Boot dependency.
- All bean configuration (factories, conditional beans, etc.) lives in infrastructure module.

---

## 7. Error Handling & Exceptions

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

## 9. Testing Architecture

1. **Unit tests**
- Domain logic tested without Spring.
- Strategies/commands have dedicated test suites.

2. **Integration tests**
- Repositories, cache, external systems.

3. **Contract tests**
- For external API integrations.

4. **End-to-end tests**
- Via main application module.

---

## 10. Conventions for AI-assisted Code Generation

To produce non-random and consistent output across tools:

- Always reference this `ARCHITECTURE.md` and `CODING_GUIDELINES.md`.
- AI-generated code must:
- State which module it belongs to.
- State how it fits the architecture.
- Warn if the prompt violates architectural constraints.
- Do not introduce additional design patterns unless justified.
