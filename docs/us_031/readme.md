# US031 — Register Backoffice Users

## 1. Requirements Engineering

### 1.1 User Story

As Administrator, I want to be able to register users of the backoffice.

The requirements also specify that this functionality must be supported through a bootstrap process.

---

## 1.2 Context

The system requires internal backoffice users responsible for administrative and operational tasks.

These users must:

* authenticate into the system;
* have authorization roles;
* possess business profile information;
* be manageable independently from framework infrastructure concerns.

The implementation therefore separates:

* authentication users (`SystemUser`);
* business profile users (`BackofficeUser`).

This separation aligns the solution with DDD principles and improves modularity.

---

## 1.3 Acceptance Criteria

* Only administrators can register backoffice users.
* Usernames must be unique.
* Emails must be unique.
* Passwords must comply with the project password policy.
* Registered users must receive the correct backoffice role.
* Registration must create both authentication and business profile information.
* The system must support bootstrap creation of default users.

---

## 1.4 Dependencies

This user story depends on:

* US030 Authentication and Authorization;
* repository infrastructure;
* persistence configuration;
* EAPLI Authz framework.

---

# 2. Analysis

The registration of backoffice users is an administrative operation that combines:

* security concerns;
* authentication infrastructure;
* business profile management.

The project uses:

* `SystemUser` from the EAPLI Authz framework for authentication and authorization;
* `BackofficeUser` as a business profile entity.

This separation avoids mixing framework concerns with domain information.

The registration process must:

* validate uniqueness;
* validate password policy;
* create authentication credentials;
* create the corresponding business profile.

---

# 3. Design

## 3.1 Architectural Approach

The implementation follows the project layered architecture:

```text id="yo6s5u"
Presentation -> Application -> Domain -> Infrastructure
```

Responsibilities:

| Layer          | Responsibility                                 |
| -------------- | ---------------------------------------------- |
| Presentation   | Collect input and show results                 |
| Application    | Coordinate registration                        |
| Domain         | Business validation and profile representation |
| Infrastructure | Persistence and bootstrap integration          |

The implementation follows EAPLI architectural recommendations and repository abstraction principles.

---

## 3.2 Main Components

### Presentation Layer

Package:

```text id="5m8g2v"
pt.isep.sem4pi.presentation.usermanagement
```

Main component:

```text id="z50g71"
RegisterBackofficeUserUI
```

Responsibilities:

* collect registration data;
* validate basic input formatting;
* delegate registration to the controller;
* present operation results.

The UI does not access repositories directly.

---

### Application Layer

Package:

```text id="mjlwm7"
pt.isep.sem4pi.application.controller.usermanagement
```

Main component:

```text id="8g1b7n"
RegisterBackofficeUserController
```

Responsibilities:

* coordinate registration;
* validate application-level rules;
* delegate persistence operations to services.

The controller keeps the UI thin and centralizes use case orchestration.

---

### Application Service Layer

Package:

```text id="k5ibdb"
pt.isep.sem4pi.application.service.usermanagment
```

Main component:

```text id="n9xqwy"
RegisterBackofficeUserService
```

Responsibilities:

* validate uniqueness;
* coordinate repositories;
* create framework authentication users;
* create business profile users.

The service centralizes application coordination logic.

---

### Domain Layer

Package:

```text id="1dk2b4"
pt.isep.sem4pi.domain.usermanagement
```

Main components:

* `BackofficeUser`
* `SEM4PIPasswordPolicy`
* `SEM4PIRoles`
* `UserBuilderHelper`

Responsibilities:

* represent business profile information;
* define password policy;
* define project roles;
* configure framework user creation.

---

### Infrastructure Layer

Packages:

```text id="whf7nh"
pt.isep.sem4pi.infrastructure.persistence
pt.isep.sem4pi.infrastructure.bootstrap
```

Responsibilities:

* repository access;
* persistence abstraction;
* bootstrap creation of default users.

---

## 3.3 Registration Flow

General flow:

```text id="zxxu2i"
RegisterBackofficeUserUI
 -> RegisterBackofficeUserController
 -> RegisterBackofficeUserService
 -> UserRepository
 -> BackofficeUserRepository
```

The service coordinates:

* authentication user creation;
* business profile creation.

---

## 3.4 Separation Between SystemUser and BackofficeUser

The implementation separates:

* framework authentication entities;
* business profile entities.

### SystemUser

Managed by the EAPLI framework.

Responsibilities:

* authentication;
* authorization;
* active/inactive status;
* session integration.

---

### BackofficeUser

Business profile entity.

Responsibilities:

* business-related user information;
* contact information;
* security clearance validity;
* skills assessment date.

This separation improves:

* modularity;
* reuse;
* maintainability;
* framework independence.

---

## 3.5 Validation Rules

The implementation validates:

* username uniqueness;
* email uniqueness;
* password policy;
* required fields.

Uniqueness is verified against:

* `BackofficeUserRepository`;
* `UserRepository`.

This avoids inconsistent registration states.

---

## 3.6 Password Policy Integration

Password validation is delegated to:

```text id="jjlwmk"
SEM4PIPasswordPolicy
```

The policy integrates directly with:

```text id="bjpmdu"
UserBuilderHelper
```

This guarantees centralized password validation during user creation.

---

## 3.7 Bootstrap Process

The requirements specify that registration must also be supported through bootstrap.

The implementation includes:

```text id="u0l7yq"
MasterUsersBootstrapper
```

Responsibilities:

* create default administrator;
* create default backoffice user;
* avoid duplicate insertions.



The bootstrap checks repository state before insertion:

* preventing duplicate users;
* supporting repeated application initialization.

---

## 3.8 Design Decisions

### Use of Separate Business Profile Entity

`BackofficeUser` was implemented separately from `SystemUser` because:

* framework entities should not contain business-specific data;
* business information may evolve independently;
* it improves domain clarity.

---

### Application Service Coordination

The registration logic was centralized in:

```text id="r8wqhz"
RegisterBackofficeUserService
```

This prevents:

* duplicated orchestration logic;
* repository access from the UI;
* excessive controller responsibilities.

---

### Repository Abstraction

Repositories are accessed through interfaces and `PersistenceContext`.

This allows:

* in-memory persistence;
* JPA persistence.

without changing business logic.

---

# 4. Architecture

## 4.1 Repository Infrastructure

The implementation uses:

* repository interfaces;
* repository factories;
* persistence abstraction.

Repositories are accessed through:

```text id="dyyvms"
PersistenceContext
```

This follows Dependency Inversion principles.

---

## 4.2 Persistence Strategy

The project supports:

* in-memory repositories;
* JPA repositories.

The default implementation for these user stories uses:

```text id="n93ifj"
InMemoryRepositoryFactory
```



This implementation also seeds default users for simplified initialization and testing.

---

## 4.3 Concurrency

Concurrency support is handled through optimistic locking using JPA `@Version` fields.

This avoids conflicting concurrent updates while preserving system performance.

---

# 5. Tests and Implementation

The implementation includes tests for:

* registration controller;
* registration service;
* password policy;
* role validation;
* repositories;
* bootstrap users;
* duplicate username validation;
* duplicate email validation.

Main tested scenarios:

* successful registration;
* duplicated usernames;
* duplicated emails;
* invalid passwords;
* repository persistence behavior;
* bootstrap initialization.

The tests use:

* JUnit 4;
* in-memory repositories;
* isolated Authz configuration.

---

# 6. Conclusion

The implementation successfully supports the registration of backoffice users while maintaining:

* separation between authentication and business concerns;
* layered architecture;
* repository abstraction;
* centralized validation.

The solution integrates naturally with the EAPLI Authz framework and provides reusable infrastructure for future user management functionalities.
