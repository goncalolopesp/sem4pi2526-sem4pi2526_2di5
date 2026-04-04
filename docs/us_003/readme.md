# US003 - Project Structure

## 1. Context

This is the first time this task is being developed.

The goal of this User Story is to define and configure the project structure to support the system architecture and the technologies required for the project.

This structure will be the foundation for all future development.

### 1.1 List of issues

Analysis:
- Study of Maven standard structure
- Identification of required layers (domain, application, etc.)

Design:
- Definition of layered architecture
- Definition of package organization

Implement:
- Creation of folder structure

Test:
- Validation of structure compliance with requirements

---

## 2. Requirements

**US003** As Project Manager, I want the team to configure the project structure to facilitate the development of upcoming user stories.

**Acceptance Criteria:**

- US003.1 The project follows the standard Maven structure  
- US003.2 The structure supports the system architecture  
- US003.3 The structure supports future development  
- US003.4 The structure is documented  

**Dependencies/References:**

- US001 - Technical Constraints  
- US010 - Domain Model  

---

## 3. Analysis

The project requires a structure that supports:

- Domain modeling (DDD approach)
- Future implementation of DSL (ANTLR)
- Simulation components
- Separation of concerns

The standard Maven structure was selected as it is required by the project and ensures compatibility with build tools and CI.

Additionally, a layered architecture was chosen to:

- Improve maintainability
- Allow scalability
- Separate business logic from infrastructure

---

## 4. Design

### 4.1. Project Structure

The adopted structure is:

```

src/
├── main/
│   ├── java/
│   │   └── pt/isep/sem4pi/
│   │        ├── domain/
│   │        ├── application/
│   │        ├── infrastructure/
│   │        ├── controllers/
│   │        └── services/
│   └── resources/
└── test/
└── java/

```

### 4.2. Architectural Decisions

- **Domain** → core business logic  
- **Application** → use case coordination  
- **Infrastructure** → persistence and external systems  
- **Controllers** → interaction layer  
- **Services** → auxiliary operations  

---

### 4.3 Package Naming

The base package `pt.isep.sem4pi` follows standard Java conventions using reverse domain naming.

This approach ensures:

- Uniqueness of the package namespace  
- Clear identification of the project context  
- Alignment with best practices in Java project organization  

The use of `pt.isep` reflects the academic institution, while `sem4pi` identifies the specific project.

---

## 5. Implementation

The structure was implemented by creating the required directories following Maven conventions.

Initial placeholder classes were added to validate the structure.

The project is now ready to support future development.

---

## 6. Integration/Demonstration

The defined structure integrates with:

- Maven build system  
- Continuous Integration (GitHub Actions)  
- Future modules such as DSL processing and simulation  

The structure allows new components to be added without breaking existing code.

---

## 7. Observations

This structure is designed to support future complexity of the system.

Alternative simpler structures were considered but rejected due to lack of scalability and poor separation of concerns.