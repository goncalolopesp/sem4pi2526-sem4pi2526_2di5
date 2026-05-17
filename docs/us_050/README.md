# US050 — Register an Air Control Area

## 1. Requirements Engineering

### 1.1 User Story

As a Backoffice Operator, I want to register an air control area. The area code must be unique in the system. Geographic boundaries must be valid

---

## 1.2 Context

Air Control Areas define geographic regions of controlled airspace. Each area is identified by a unique code and is bounded by latitude and longitude limits. A minimum reserve fuel rule is associated with each area to enforce safety regulations.

Registering Air Control Areas is a prerequisite for other functionalities that depend on airspace boundaries, such as flight plan validation.

---

## 1.3 Acceptance Criteria

* The Backoffice Operator must be authenticated and authorized to perform this operation.
* An Air Control Area must be uniquely identified by its code.
* The system must reject registration if an area with the same code already exists.
* Latitude values must be between -90 and 90, with min strictly less than max.
* Longitude values must be between -180 and 180, with min strictly less than max.
* The minimum reserve fuel rule must be zero or greater.
* The name of the area cannot be null or empty.
* On success, the system must persist the new area and confirm registration to the operator.

---

## 1.4 Dependencies

This user story depends on:

* authentication and authorization infrastructure (US030);
* repository infrastructure and persistence configuration;
* bootstrap process.

---

# 2. Analysis

The registration of an Air Control Area is a straightforward creation use case. The primary concerns are:

* uniqueness validation by area code;
* boundary value validation (latitude and longitude ranges);
* minimum reserve fuel rule validation;
* authorization enforcement — only authenticated Backoffice Operators or Admins may register areas.

The domain model was designed to encapsulate all validation within the `AirControlArea` aggregate and its associated value objects (`AirControlAreaID`, `AirControlAreaBoundary`), keeping the controller thin and delegating business rules to the domain layer.

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
pt.isep.sem4pi.presentation.aircontrolarea
```

Main component:

* `RegisterAirControlAreaUI`

Responsibilities:

* collect area data from the operator (code, name, boundaries, minimum reserve fuel);
* present results and error messages;
* delegate operation to the controller.

The UI layer does not directly access repositories or apply business validation.

---

### Application Layer

Package:

```
pt.isep.sem4pi.application.controller.aircontrolarea
```

Main component:

```
RegisterAirControlAreaController
```

Responsibilities:

* enforce authorization before proceeding;
* check uniqueness of the area code;
* coordinate the creation of domain objects;
* persist the new area through the repository.

The controller is annotated with `@UseCaseController` following the GRASP Controller principle.

---

### Domain Layer

Packages:

```
pt.isep.sem4pi.domain.aircontrolarea
pt.isep.sem4pi.domain.repositories
```

Main components:

* `AirControlArea` — aggregate root;
* `AirControlAreaID` — value object, unique identifier;
* `AirControlAreaBoundary` — value object, encapsulates boundary validation;
* `AirControlAreaRepository` — repository interface.

The `AirControlArea` aggregate enforces all invariants at construction time using `Preconditions`. The `AirControlAreaBoundary` validates latitude and longitude ranges independently.

---

### Infrastructure Layer

Packages:

```
pt.isep.sem4pi.infrastructure.persistence.JPA
pt.isep.sem4pi.infrastructure.persistence.memory
```

Main components:

* `JpaAirControlAreaRepository` — JPA-backed repository;
* `InMemoryAirControlAreaRepository` — in-memory repository for testing.

Both implement the `AirControlAreaRepository` interface. Switching between them does not require changes to the application layer.

---

## 3.3 Registration Flow

```
RegisterAirControlAreaUI
 -> RegisterAirControlAreaController
 -> AuthorizationService (authorization check)
 -> AirControlAreaRepository (duplicate check)
 -> AirControlAreaBoundary (boundary validation)
 -> AirControlArea (aggregate creation)
 -> AirControlAreaRepository (persistence)
```

---

## 3.4 Design Decisions

### Authorization at the Controller Level

Authorization is enforced at the start of the controller method via:

```
authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR, SEM4PIRoles.ADMIN);
```

This ensures that unauthenticated or unauthorized access is rejected before any domain logic is executed.

---

### Uniqueness Check Before Domain Object Creation

The duplicate check is performed before creating domain objects:

```
if (repository.containsOfIdentity(id)) {
    throw new IllegalArgumentException("An Air Control Area with the code already exists.");
}
```

This avoids unnecessary object creation and makes the failure reason explicit.

---

### Value Objects Encapsulate Validation

Boundary and identifier validation are encapsulated in `AirControlAreaBoundary` and `AirControlAreaID` respectively, keeping the aggregate constructor clean and ensuring that invalid data never enters the domain.

---

### Repository Abstraction via PersistenceContext

Repositories are accessed via `PersistenceContext`, allowing the implementation to switch between JPA and in-memory without changes to the controller.

---

# 4. Architecture

## 4.1 Domain Model

The `AirControlArea` aggregate root contains:

* `AirControlAreaID` — embedded value object used as the primary key;
* `AirControlAreaBoundary` — embedded value object with latitude and longitude ranges;
* `name` — descriptive name of the area;
* `minimumReserveFuelRule` — minimum fuel reserve required within the area;
* `version` — JPA optimistic locking field.

---

## 4.2 Repository Interface

```
public interface AirControlAreaRepository extends DomainRepository<AirControlAreaID, AirControlArea> {
    default boolean containsOfIdentity(AirControlAreaID id) {
        return ofIdentity(id).isPresent();
    }
}
```

The default method `containsOfIdentity` is provided to support the duplicate check without requiring any additional query.

---

## 4.3 Concurrency

Optimistic locking is applied through the `@Version` field in `AirControlArea`, preventing conflicting concurrent updates.

---

# 5. Tests and Implementation

The implementation includes tests for:

* successful registration of a new Air Control Area;
* rejection when the area code already exists;
* rejection when boundary values are invalid;
* rejection when minimum reserve fuel is negative;
* rejection when name is null or empty;
* authorization enforcement (unauthorized access rejected).

Tests use:

* JUnit;
* `InMemoryAirControlAreaRepository`;
* isolated `AuthorizationService` configuration.

---

# 6. Conclusion

The implementation successfully registers Air Control Areas following:

* layered architecture principles;
* DDD aggregate and value object patterns;
* repository abstraction for persistence independence;
* EAPLI authorization integration for access control.

The domain model ensures all invariants are enforced before any area is persisted.