# **US003 - Project Structure**

## 1. Context

This is the first time this task is being developed.

The goal of this User Story is to define and configure the project structure to support the system architecture and the technologies required for the project.

This structure will serve as the foundation for all future development.

---

### 1.1 List of Issues

**Analysis:**

* Study of Maven standard structure
* Identification of required architectural layers

**Design:**

* Definition of layered architecture
* Definition of package organization

**Implementation:**

* Creation of folder structure

**Test:**

* Validation of structure compliance with requirements

---

## 2. Requirements

**US003**
As Project Manager, I want the team to configure the project structure to facilitate the development of upcoming user stories.

### Acceptance Criteria

* US003.1 The project follows the standard Maven structure
* US003.2 The structure supports the system architecture
* US003.3 The structure supports future development
* US003.4 The structure is documented

---

### Dependencies / References

* US001 – Technical Constraints
* US010 – Domain Model

---

## 3. Analysis

The project requires a structure that supports:

* Domain modeling using a DDD approach
* Separation of concerns across layers
* Future components such as DSL processing and simulation modules
* Maintainability and scalability of the system

The standard Maven structure was selected as it ensures compatibility with build tools and continuous integration processes.

---

## 4. Design

### 4.1 Project Structure

The adopted structure is:

```
src/
├── main/
│   ├── java/
│   │   └── pt/isep/sem4pi/
│   │        ├── domain/
│   │        ├── application/
│   │        ├── infrastructure/
│   │        └── presentation/
│   └── resources/
└── test/
    └── java/
```

---

### 4.2 Architectural Decisions

The system follows a **layered architecture**, separating responsibilities into distinct layers:

* **Domain** → Core business logic, entities, and value objects
* **Application** → Use case coordination and orchestration of domain logic
* **Infrastructure** → Technical concerns such as persistence and external integrations
* **Presentation** → User interaction layer (console UI and input/output handling)

This approach ensures:

* High cohesion within each layer
* Low coupling between layers
* Easier maintenance and evolution of the system

---

### 4.3 Package Naming

The base package `pt.isep.sem4pi` follows the reverse domain naming convention commonly used in Java.

This ensures:

* Uniqueness of the package namespace
* Clear identification of the project context
* Alignment with Java best practices

The use of `pt.isep` reflects the academic institution, while `sem4pi` identifies the project.

---

## 5. Implementation

The structure was implemented by creating the required directories following Maven conventions.

Each layer was created as a separate package to enforce the architectural separation defined in the design phase.

The project is now prepared to support the development of future user stories.

---

## 6. Integration / Demonstration

The defined structure integrates with:

* Maven build system
* Continuous Integration (GitHub Actions)
* Future system components such as DSL processing and simulation modules

The separation of layers ensures that new features can be added without affecting existing components.

---

## 7. Observations

The selected structure was designed to support the expected complexity of the system.

Alternative simpler structures were considered but rejected, as they did not provide sufficient scalability nor ensure a proper separation of concerns required for the system.