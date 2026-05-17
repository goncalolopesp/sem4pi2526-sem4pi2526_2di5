# US032 — Disable/Enable Backoffice Users

## 1. Requirements Engineering

### 1.1 User Story

As Administrator, I want to be able to disable/enable users of the backoffice.

---

## 1.2 Context

The system requires administrative control over access permissions for internal backoffice users.

Administrators must be able to:

* temporarily disable users;
* re-enable previously disabled users;
* prevent disabled users from authenticating into the system.

The implementation integrates:

* authorization validation;
* user status management;
* authentication infrastructure.

The solution uses the active state already provided by the EAPLI `SystemUser` entity instead of duplicating status management inside the business profile entity.

---

## 1.3 Acceptance Criteria

* Only administrators can enable or disable backoffice users.
* Disabled users must not authenticate into the system.
* Enabled users must regain access to the system.
* User status operations must update the authentication entity state.
* Administrators must not disable their own active session.
* The system must support listing users together with their current status.

---

## 1.4 Dependencies

This user story depends on:

* US030 Authentication and Authorization;
* US031 Register Backoffice Users;
* repository infrastructure;
* EAPLI Authz framework.

---

# 2. Analysis

The enable/disable operation is an administrative responsibility directly related to authentication access control.

The implementation needed to:

* reuse the existing `SystemUser.active` state from the EAPLI framework;
* avoid duplicated user status information;
* protect administrative operations through authorization;
* maintain consistency between authentication and business profile data.

The system therefore separates:

* authentication state (`SystemUser`);
* business profile information (`BackofficeUser`).

This avoids synchronization problems between duplicated user status fields.

---

# 3. Design

## 3.1 Architectural Approach

The implementation follows the project layered architecture:

```text id="vhc2d3"
Presentation -> Application -> Domain -> Infrastructure
```

Responsibilities:

| Layer          | Responsibility                            |
| -------------- | ----------------------------------------- |
| Presentation   | User interaction                          |
| Application    | Coordination of administrative operations |
| Domain         | Business validation and role definitions  |
| Infrastructure | Persistence and framework integration     |

This structure follows EAPLI architectural guidelines and repository abstraction principles.

---

## 3.2 Main Components

### Presentation Layer

Package:

```text id="9h5p7s"
pt.isep.sem4pi.presentation.usermanagement
```

Main components:

* `ManageBackofficeUserStatusUI`
* `ListBackofficeUsersUI`

Responsibilities:

* present user lists;
* collect administrator actions;
* delegate operations to application services/controllers.




---

### Application Layer

Package:

```text id="g9v4r1"
pt.isep.sem4pi.application.service.usermanagment
```

Main component:

```text id="l9o7qr"
UserStatusService
```

Responsibilities:

* validate administrator permissions;
* list users and status;
* enable users;
* disable users;
* prevent invalid operations.



---

### Domain Layer

Package:

```text id="ll1vh2"
pt.isep.sem4pi.domain.usermanagement
```

Main components:

* `SEM4PIRoles`
* `BackofficeUser`

Responsibilities:

* represent business user profiles;
* define administrative roles.

---

### Infrastructure Layer

Packages:

```text id="tbrlmf"
pt.isep.sem4pi.infrastructure.persistence
pt.isep.sem4pi.infrastructure.bootstrap
```

Responsibilities:

* repository access;
* persistence abstraction;
* framework integration.

---

## 3.3 User Status Management Strategy

The implementation uses the active state already available in:

```text id="ib7o4i"
SystemUser
```

instead of storing duplicated status information inside:

```text id="nhk40n"
BackofficeUser
```

This design avoids:

* duplicated state;
* synchronization inconsistencies;
* unnecessary persistence complexity.

The business profile entity remains focused on business information only.

---

## 3.4 Enable/Disable Flow

General flow:

```text id="4jlwm0"
ManageBackofficeUserStatusUI
 -> UserStatusService
 -> UserRepository
 -> SystemUser
```

The service:

* validates administrator permissions;
* loads the authentication user;
* changes active state;
* persists the updated user.

---

## 3.5 Authorization Validation

Administrative operations require:

```text id="zmv7xe"
ADMIN
```

permissions.

Authorization validation is centralized in:

```text id="ud9goh"
ensureAdmin()
```

inside:

```text id="e3nln8"
UserStatusService
```



This guarantees that:

* unauthorized users cannot manage accounts;
* security checks are centralized.

---

## 3.6 Self-Disable Protection

The implementation prevents administrators from disabling their own authenticated session.

This validation avoids:

* accidental lockout situations;
* loss of administrative access.

Validation logic:

```text id="75ixgi"
Cannot disable the currently logged-in administrator
```



---

## 3.7 User Listing with Status

The implementation also supports listing users together with their current authentication state.

The service combines:

* `BackofficeUser`;
* `SystemUser.active`.

through:

```text id="tb8k6x"
BackofficeUserStatus
```

This avoids exposing framework entities directly to the presentation layer.

---

## 3.8 Design Decisions

### Reuse of SystemUser Active State

The project reuses the active state already provided by the EAPLI framework instead of introducing a second status field.

Advantages:

* reduced duplication;
* improved consistency;
* simpler persistence model;
* direct integration with authentication.

---

### Centralized Administrative Validation

Administrative authorization checks were centralized in:

```text id="wn8q9j"
UserStatusService
```

This avoids:

* duplicated security logic;
* inconsistent authorization validation.

---

### Separation Between Authentication and Business Profiles

The implementation preserves the separation between:

* authentication infrastructure;
* business profile information.

This improves modularity and domain clarity.

---

# 4. Architecture

## 4.1 Repository Infrastructure

Repositories are accessed through:

```text id="1s3cg9"
PersistenceContext
```

The solution supports:

* in-memory repositories;
* JPA repositories.

without changing application logic.

---

## 4.2 Persistence Strategy

The implementation uses:

* repository interfaces;
* repository factories;
* dependency inversion.

The default implementation for these user stories uses:

```text id="l1v2pb"
InMemoryRepositoryFactory
```



---

## 4.3 Concurrency

Concurrency handling relies on optimistic locking through JPA `@Version` fields.

This approach:

* avoids pessimistic locking;
* improves concurrency performance;
* protects concurrent updates.

---

# 5. Tests and Implementation

The implementation includes tests for:

* enabling users;
* disabling users;
* preventing self-disable;
* authentication denial for disabled users;
* repository persistence;
* listing users with status.

Main tested scenarios:

* successful enable operation;
* successful disable operation;
* disabled user authentication failure;
* unauthorized access attempts;
* repository consistency.

The tests use:

* JUnit 4;
* in-memory repositories;
* isolated authentication configuration.

---

# 6. Conclusion

The implementation successfully supports administrative enable/disable management for backoffice users while preserving:

* authentication consistency;
* repository abstraction;
* centralized authorization validation;
* separation between framework infrastructure and business profile information.

The solution integrates naturally with the EAPLI authentication infrastructure and follows the layered architecture adopted throughout the project.
