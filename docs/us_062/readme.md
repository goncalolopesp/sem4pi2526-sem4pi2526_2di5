# US062 — List Collaborators of an Air Transport Company

## 1. Requirements Engineering

### 1.1 User Story

As a Backoffice Operator, I want to list all collaborators of a given customer. It should not list disabled collaborators.

---

## 1.2 Context

An Air Transport Company may have multiple collaborators — system users formally associated with it. Being able to list these collaborators is an operational necessity for backoffice management, allowing operators to verify and audit company associations.

Only active collaborators are presented, since inactive system users should not be visible in operational listings.

---

## 1.3 Acceptance Criteria

* The Backoffice Operator must be authenticated and authorized to perform this operation.
* The operator must provide the ICAO code of the company to look up.
* The system must reject the request if no company is found for the given ICAO code.
* Only collaborators whose associated system user is active must be returned.
* Each collaborator entry must include: username, first name, last name, and email.
* If the company has no active collaborators, an empty list is returned.

---

## 1.4 Dependencies

This user story depends on:

* authentication and authorization infrastructure (US030);
* registration of Air Transport Companies;
* addition of Air Transport Company Collaborators (US061);
* repository infrastructure and persistence configuration.

---

# 2. Analysis

Listing collaborators is a query use case: no state is modified. The controller retrieves the company by ICAO code, fetches all associated collaborators, filters out inactive users, and returns a projection (`CollaboratorView`) with only the data needed by the presentation layer.

The use of a dedicated `CollaboratorView` record decouples the presentation layer from the domain model, avoiding direct exposure of `SystemUser` or `AirTransportCompanyCollaborator` internals to the UI.

The filtering of inactive users is performed in-memory after retrieval, keeping the repository interface simple and avoiding leaking business rules into query predicates.

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
| Application    | Use case coordination and projection  |
| Domain         | Business rules and model              |
| Infrastructure | Persistence and framework integration |

---

## 3.2 Main Components

### Presentation Layer

Package:

```
pt.isep.sem4pi.presentation.airtransportcompany
```

Main component:

* `ListCustomerCollaboratorsUI`

Responsibilities:

* collect the ICAO code from the operator;
* display the returned list of collaborators;
* present error messages when applicable.

---

### Application Layer

Package:

```
pt.isep.sem4pi.application.controller.airtransportcompany
```

Main component:

```
ListCustomerCollaboratorsController
```

Responsibilities:

* enforce authorization;
* resolve the Air Transport Company by ICAO code;
* retrieve all collaborators of that company;
* filter out inactive system users;
* project each collaborator into a `CollaboratorView` record;
* return the list to the UI.

The `CollaboratorView` record is defined as an inner record of the controller:

```
public record CollaboratorView(String username, String firstName, String lastName, String email) {}
```

---

### Domain Layer

Packages:

```
pt.isep.sem4pi.domain.model.airtransportcompany
pt.isep.sem4pi.domain.repositories
```

Main components referenced:

* `AirTransportCompany` — aggregate looked up by ICAO code;
* `AirTransportCompanyCollaborator` — entity providing access to the associated `SystemUser`;
* `IcaoCode` — value object used as the lookup key;
* `AirTransportCompanyRepository` — provides `findByIcaoCode`;
* `AirTransportCompanyCollaboratorRepository` — provides `findByCompany`.

---

### Infrastructure Layer

Packages:

```
pt.isep.sem4pi.infrastructure.persistence.JPA
pt.isep.sem4pi.infrastructure.persistence.memory
```

The same repository implementations used in US061 are reused here. No additional infrastructure components are required.

---

## 3.3 Listing Flow

```
ListCustomerCollaboratorsUI
 -> ListCustomerCollaboratorsController
 -> AuthorizationService (authorization check)
 -> AirTransportCompanyRepository (resolve company by ICAO code)
 -> AirTransportCompanyCollaboratorRepository (find collaborators by company)
 -> SystemUser (filter inactive, extract projection data)
 -> List<CollaboratorView> (returned to UI)
```

---

## 3.4 Design Decisions

### Projection via `CollaboratorView` Record

Rather than exposing domain objects to the presentation layer, the controller maps each collaborator to a `CollaboratorView` record containing only the fields needed for display. This follows the principle of minimal exposure and avoids coupling the UI to domain internals.

---

### Inactive User Filtering in the Application Layer

The filtering of inactive users is performed by the controller after repository retrieval:

```
if (!user.isActive()) {
    continue;
}
```

This keeps the `AirTransportCompanyCollaboratorRepository` interface simple and avoids encoding business filtering logic into JPQL queries, which would reduce testability and portability.

---

### Authorization at the Controller Level

Authorization is enforced at the start of the operation:

```
authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.ADMIN, SEM4PIRoles.BACKOFFICE_OPERATOR);
```

Both `ADMIN` and `BACKOFFICE_OPERATOR` roles are permitted, consistent with the access policy applied across Air Transport Company management use cases.

---

### Reuse of Existing Repository Interface

No new repository methods are required. `findByCompany` was already defined in `AirTransportCompanyCollaboratorRepository` as part of US061, and is directly reused here.

---

# 4. Architecture

## 4.1 Query Flow Summary

The operation is read-only. No aggregate is modified. The controller exclusively reads from repositories and projects the results, making this use case safe for concurrent execution without locking concerns.

---

## 4.2 Repository Methods Used

| Repository                                  | Method                        | Purpose                            |
| ------------------------------------------- | ----------------------------- | ---------------------------------- |
| `AirTransportCompanyRepository`             | `findByIcaoCode(icaoCode)`    | Resolve company by ICAO code       |
| `AirTransportCompanyCollaboratorRepository` | `findByCompany(company)`      | Retrieve all collaborators         |

---

# 5. Tests and Implementation

The implementation includes tests for:

* successful listing of active collaborators for a valid company;
* empty list returned when all collaborators are inactive;
* empty list returned when the company has no collaborators;
* rejection when the company ICAO code does not exist;
* authorization enforcement (unauthorized access rejected).

Tests use:

* JUnit;
* `InMemoryAirTransportCompanyCollaboratorRepository`;
* `InMemoryAirTransportCompanyRepository`;
* isolated `AuthorizationService` configuration.

---

# 6. Conclusion

The implementation successfully lists active collaborators of an Air Transport Company following:

* layered architecture principles;
* projection pattern to decouple domain from presentation;
* reuse of repository infrastructure defined in US061;
* EAPLI authorization integration for access control.

The read-only nature of this use case ensures no side effects and simplifies concurrency handling.