# US061 — Add Air Transport Company Collaborator

## 1. Requirements Engineering

### 1.1 User Story

As a Backoffice Operator, I want to associate a system user as a collaborator of an Air Transport Company so that they can act on behalf of that company within the system.

---

## 1.2 Context

An Air Transport Company Collaborator is a system user who is formally linked to an Air Transport Company. This association enables role-specific operations tied to a particular company throughout the system.

Before collaborators can be added, both system users and Air Transport Companies must already exist in the system.

---

## 1.3 Acceptance Criteria

* The Backoffice Operator must be authenticated and authorized to perform this operation.
* The selected system user must exist in the system.
* The selected Air Transport Company must exist in the system.
* A system user can only be a collaborator of one company at a time.
* The system must reject the operation if the user is already registered as a collaborator.
* On success, the system must persist the association and confirm it to the operator.

---

## 1.4 Dependencies

This user story depends on:

* authentication and authorization infrastructure (US030);
* registration of Air Transport Companies;
* system user management;
* repository infrastructure and persistence configuration.

---

# 2. Analysis

Adding a collaborator is an association use case: it links an existing system user to an existing Air Transport Company.

The primary concerns are:

* authorization enforcement — only authenticated Backoffice Operators or Admins may perform this operation;
* uniqueness of the collaborator per system user — a user cannot be associated with more than one company;
* both the system user and company must exist and be resolvable from their respective repositories.

The domain entity `AirTransportCompanyCollaborator` is the aggregate root for this association, holding references to both the `SystemUser` and the `AirTransportCompany`.

---

# 3. Design

## 3.1 Architectural Approach

The implementation follows the layered architecture adopted in the project:

```
Presentation -> Application -> Domain -> Infrastructure
```

Responsibilities:

| Layer          | Responsibility                        |
| -------------- | ------------------------------------- |
| Presentation   | User interaction                      |
| Application    | Use case coordination                 |
| Domain         | Business rules and policies           |
| Infrastructure | Persistence and framework integration |

---

## 3.2 Main Components

### Presentation Layer

Package:

```
pt.isep.sem4pi.presentation.airtransportcompany
```

Main component:

* `AddAirTransportCompanyCollaboratorUI`

Responsibilities:

* present lists of available system users and Air Transport Companies;
* collect the operator's selections;
* present confirmation or error messages;
* delegate operation to the controller.

---

### Application Layer

Package:

```
pt.isep.sem4pi.application.controller.airtransportcompany
```

Main component:

```
AddAirTransportCompanyCollaboratorController
```

Responsibilities:

* enforce authorization;
* provide the list of system users for selection;
* provide the list of Air Transport Companies for selection;
* check whether the selected user is already a collaborator;
* coordinate the creation and persistence of the collaborator association.

---

### Domain Layer

Packages:

```
pt.isep.sem4pi.domain.model.airtransportcompany
pt.isep.sem4pi.domain.repositories
```

Main components:

* `AirTransportCompanyCollaborator` — aggregate root;
* `AirTransportCompany` — referenced aggregate;
* `AirTransportCompanyCollaboratorRepository` — repository interface.

The `AirTransportCompanyCollaborator` entity enforces that neither the `SystemUser` nor the `AirTransportCompany` can be null, through a guard in its constructor.

---

### Infrastructure Layer

Packages:

```
pt.isep.sem4pi.infrastructure.persistence.JPA
pt.isep.sem4pi.infrastructure.persistence.memory
```

Main components:

* `JpaAirTransportCompanyCollaboratorRepository` — JPA-backed repository;
* `InMemoryAirTransportCompanyCollaboratorRepository` — in-memory repository for testing, with auto-assigned sequential identifiers.

Both implement the `AirTransportCompanyCollaboratorRepository` interface.

---

## 3.3 Addition Flow

```
AddAirTransportCompanyCollaboratorUI
 -> AddAirTransportCompanyCollaboratorController
 -> AuthorizationService (authorization check)
 -> UserRepository (list users)
 -> AirTransportCompanyRepository (list companies)
 -> AirTransportCompanyCollaboratorRepository (duplicate check)
 -> AirTransportCompanyCollaborator (entity creation)
 -> AirTransportCompanyCollaboratorRepository (persistence)
```

---

## 3.4 Design Decisions

### Authorization at the Controller Level

Authorization is enforced at the start of each controller method, ensuring unauthenticated or unauthorized users are rejected before any domain logic executes.

---

### Uniqueness Per System User

Before creating a new collaborator, the controller queries:

```
collaboratorRepository.findBySystemUser(systemUser)
```

If a result is present, the operation is rejected with a clear error message. This enforces the business rule that a system user can only be collaborator of one company.

---

### Auto-assigned Identity in In-Memory Repository

Because `AirTransportCompanyCollaborator` uses a database-generated `Long` identity (`@GeneratedValue`), the in-memory repository simulates this with an `AtomicLong` counter:

```
entity.assignIdentity(nextId.getAndIncrement());
```

This allows the in-memory repository to behave consistently with the JPA implementation during testing.

---

### Repository Abstraction via PersistenceContext

All repositories are accessed through `PersistenceContext`, preserving independence between the application layer and the persistence implementation.

---

# 4. Architecture

## 4.1 Domain Model

The `AirTransportCompanyCollaborator` entity contains:

* `id` — auto-generated Long, serves as the aggregate identity;
* `systemUser` — `@OneToOne` reference to the EAPLI `SystemUser`;
* `company` — `@ManyToOne` reference to the `AirTransportCompany`.

---

## 4.2 Repository Interface

```
public interface AirTransportCompanyCollaboratorRepository
        extends DomainRepository<Long, AirTransportCompanyCollaborator> {

    Optional<AirTransportCompanyCollaborator> findBySystemUser(SystemUser systemUser);

    Iterable<AirTransportCompanyCollaborator> findByCompany(AirTransportCompany company);
}
```

`findBySystemUser` is used for the uniqueness check. `findByCompany` supports listing all collaborators of a given company.

---

## 4.3 Concurrency

For JPA, optimistic locking can be added to `AirTransportCompanyCollaborator` via a `@Version` field if concurrent modification becomes a concern. The current implementation relies on the database constraint on the `@OneToOne` relationship with `SystemUser` to prevent duplicates at the persistence level.

---

# 5. Tests and Implementation

The implementation includes tests for:

* successful creation of a collaborator association;
* rejection when the system user is already a collaborator;
* rejection when system user or company is null;
* authorization enforcement (unauthorized access rejected);
* `findBySystemUser` and `findByCompany` repository query correctness.

Tests use:

* JUnit;
* `InMemoryAirTransportCompanyCollaboratorRepository`;
* isolated `AuthorizationService` configuration.

---

# 6. Conclusion

The implementation successfully associates system users with Air Transport Companies following:

* layered architecture principles;
* DDD entity patterns;
* repository abstraction for persistence independence;
* EAPLI authorization integration for access control.

The uniqueness constraint ensures data integrity across the collaborator association lifecycle.