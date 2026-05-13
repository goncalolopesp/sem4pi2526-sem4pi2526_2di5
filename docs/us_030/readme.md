# US030 — Authentication and Authorization

## 1. Requirements Engineering

### 1.1 User Story

As a Project Manager, I want the system to support and apply authentication and authorization for all its users and functionalities.

---

## 1.2 Context

The system contains multiple user roles and protected functionalities.
Authentication and authorization are transversal concerns required by all remaining user stories involving protected access.

The project requirements explicitly recommend the usage of existing frameworks for authentication and authorization, since these concerns are infrastructure-oriented and not the main business focus of the system.

---

## 1.3 Acceptance Criteria

* Users must authenticate before accessing protected functionalities.
* The system must support login and logout operations.
* Authentication must validate username and password.
* Authorization must restrict functionalities according to user roles.
* Invalid credentials must prevent access.
* Disabled users must not authenticate.
* Passwords must follow the project password policy.
* The solution must integrate with the EAPLI authorization framework.

---

## 1.4 Dependencies

This user story depends on:

* repository infrastructure;
* persistence configuration;
* bootstrap process;
* EAPLI Authz framework integration.

It also supports all remaining protected user stories.

---

# 2. Analysis

Authentication and authorization are cross-cutting concerns required throughout the system.

The implementation needed to:

* support authenticated sessions;
* support role-based authorization;
* integrate with console menus;
* remain independent from business use cases.

Because authentication is not the main business focus of the project, the solution adopts the EAPLI Authz framework, as recommended in the requirements.

The implementation separates:

* infrastructure authentication concerns;
* application orchestration;
* business-specific validation.

This separation improves maintainability and avoids spreading security logic across the system.

---

# 3. Design

## 3.1 Architectural Approach

The implementation follows the layered architecture adopted in the project:

```text
Presentation -> Application -> Domain -> Infrastructure
```

Responsibilities:

| Layer          | Responsibility                        |
| -------------- | ------------------------------------- |
| Presentation   | User interaction                      |
| Application    | Use case coordination                 |
| Domain         | Business rules and policies           |
| Infrastructure | Persistence and framework integration |

This structure follows the architectural organization recommended in the EAPLI framework and DDD slides.

---

## 3.2 Main Components

### Presentation Layer

Package:

```text
pt.isep.sem4pi.presentation.authz
```

Main components:

* `LoginUI`
* `LogoutUI`
* `ChangePasswordUI`
* `LoginAction`
* `LogoutAction`

Responsibilities:

* collect user input;
* present results;
* delegate operations to controllers.

The UI layer does not directly access repositories.

---

### Application Layer

Package:

```text
pt.isep.sem4pi.application.controller.usermanagement
```

Main component:

```text
AuthenticationController
```

Responsibilities:

* coordinate authentication;
* coordinate logout;
* coordinate password change;
* validate access permissions;
* enforce password policy integration.

The controller follows the GRASP Controller principle by centralizing the use case orchestration.

---

### Domain Layer

Package:

```text
pt.isep.sem4pi.domain.usermanagement
```

Main components:

* `SEM4PIPasswordPolicy`
* `SEM4PIRoles`
* `UserBuilderHelper`

Responsibilities:

* define valid password rules;
* define project roles;
* configure user creation with the project policies.

The password policy enforces:

* minimum length;
* uppercase character;
* numeric character.

---

### Infrastructure Layer

Packages:

```text
pt.isep.sem4pi.infrastructure.persistence
pt.isep.sem4pi.infrastructure.bootstrap
```

Responsibilities:

* repository access;
* persistence abstraction;
* framework integration;
* bootstrap users.

---

## 3.3 Authentication Flow

General authentication flow:

```text
LoginUI
 -> AuthenticationController
 -> AuthenticationService
 -> AuthorizationService
 -> UserAccessService
 -> UserRepository
```

Authentication is delegated to the EAPLI framework through:

```java
authenticationService.authenticate(...)
```



If authentication succeeds:

* the session is created;
* authorization rules are applied;
* user access validation is executed.

Disabled users are denied access through:

```text
UserAccessService
```

---

## 3.4 Authorization

Authorization is role-based.

Roles implemented:

* `ADMIN`
* `BACKOFFICE_OPERATOR`

Menus and actions are dynamically restricted according to authenticated roles.

This approach ensures:

* separation of permissions;
* protection of administrative functionalities;
* centralized authorization logic.

---

## 3.5 Session Management

Session management is handled through:

* `AuthenticationService`
* `AuthorizationService`

The implementation supports:

* authenticated sessions;
* logout;
* session clearing;
* authorization verification.

Logout flow:

```text
LogoutUI
 -> AuthenticationController
 -> AuthorizationService.clearSession()
```



---

## 3.6 Password Management

Password management is integrated into the authentication infrastructure.

The project implements:

* password validation;
* password change;
* password policy enforcement.

Password changes are coordinated through:

```text
ChangePasswordUI
 -> AuthenticationController
 -> AuthenticationService.changePassword(...)
```



The controller validates:

* authenticated session existence;
* password policy compliance.

---

## 3.7 Design Decisions

### Use of EAPLI Authz Framework

The project requirements explicitly recommend existing authentication frameworks.

The EAPLI Authz framework was adopted because:

* it already provides session handling;
* it supports role-based authorization;
* it integrates with repositories;
* it reduces infrastructure complexity;
* it aligns with EAPLI architectural guidelines.

---

### Separation Between UI and Business Logic

The UI layer only:

* collects input;
* presents results;
* delegates operations.

Business validation and orchestration are handled by controllers and services.

This avoids coupling presentation with business rules.

---

### Controller-Centered Coordination

The `AuthenticationController` centralizes:

* login;
* logout;
* password changes.

This reduces duplicated orchestration logic across UIs.

---

### Dedicated Password Policy

A custom password policy was implemented instead of hardcoded validations in the UI.

This ensures:

* centralized validation;
* reuse across the system;
* easier maintenance.

---

# 4. Architecture

## 4.1 Layered Architecture

The implementation follows the layered architecture used throughout the project:

* presentation;
* application;
* domain;
* infrastructure.

This separation improves:

* maintainability;
* modularity;
* testability;
* reuse.

---

## 4.2 Repository Integration

Repositories are accessed through:

```text
PersistenceContext
```

This abstraction allows switching between:

* in-memory repositories;
* JPA repositories.

without changing application logic.

---

## 4.3 Bootstrap Users

The bootstrap process automatically creates default users:

* `admin`
* `backoffice`



This ensures:

* immediate system access;
* simplified testing;
* consistent initialization.

The bootstrap verifies if users already exist before insertion to avoid duplicates.

---

## 4.4 Concurrency

Concurrency support is handled through optimistic locking using JPA `@Version` fields in persistent entities.

This approach:

* avoids pessimistic locking;
* improves concurrency performance;
* prevents conflicting updates.

The solution aligns with the concurrency recommendations presented in the EAPLI DDD slides.

---

# 5. Tests and Implementation

The implementation includes tests for:

* authentication;
* invalid authentication;
* disabled users;
* password policy validation;
* role validation;
* repository behavior;
* login UI;
* logout flow;
* password change flow.

Main tested scenarios:

* valid credentials;
* invalid credentials;
* disabled user authentication;
* password policy violations;
* session management;
* authorization checks.

The tests use:

* JUnit 4;
* in-memory repositories;
* isolated Authz configuration.

---

# 6. Conclusion

The implementation successfully integrates authentication and authorization into the system while following:

* EAPLI architectural recommendations;
* layered architecture;
* DDD principles;
* repository abstraction.

The adoption of the EAPLI Authz framework reduced infrastructure complexity and provided a reusable security foundation for the remaining user stories.