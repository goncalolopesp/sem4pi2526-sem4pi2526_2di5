# US072 — List Company Fleet

## 1. Requirements Engineering

### 1.1 User Story

As an Air Transport Company Collaborator, I want to list the aircraft fleet belonging to my company. The listing must support sorting by different attributes (registration, maker/manufacturer, model, capacity, and age).

---

## 1.2 Context

An Air Transport Company owns a fleet of aircraft. Collaborators associated with a specific company need the ability to view all aircraft registered to their organization. Being able to sort this information by various criteria (such as capacity for flight planning, or age for maintenance scheduling) is an operational necessity.

The system must automatically identify the company of the logged-in user, ensuring data privacy and preventing collaborators from viewing the fleets of rival companies.

---

## 1.3 Acceptance Criteria

* The user must be authenticated and authorized as an `AIR_TRANSPORT_COMPANY_COLLABORATOR`.
* The system must identify the user's associated company automatically via their session.
* The system must present the following sorting options:
    * a) Registration
    * b) Maker (Manufacturer)
    * c) Model
    * d) Capacity
    * e) Age
* The age of the aircraft must be calculated dynamically based on its manufacturing date, not statically stored.
* The listing must include registration, manufacturer, model, total capacity, age, and status.
* If the company has no registered aircraft, an appropriate empty-state message must be displayed.

---

## 1.4 Dependencies

This user story depends on:

* Authentication and authorization infrastructure (US030);
* Registration of Air Transport Companies and their Collaborators (US061);
* Addition of Aircraft to a company's fleet (US070 / US071).

---

# 2. Analysis

Listing the company's fleet is a read-only query use case. The controller retrieves the current user's session, looks up the corresponding `AirTransportCompanyCollaborator` profile, and accesses the `AirTransportCompany` aggregate to fetch its internal fleet list.

The sorting logic (by Registration, Maker, Model, Capacity, or Age) is applied in-memory within the Application Layer rather than delegating multiple different query requests to the database. This aligns perfectly with Domain-Driven Design (DDD) principles, since the `AirTransportCompany` aggregate already holds its full fleet.

The calculation of an aircraft's age is performed dynamically in the Domain Layer (inside the `Aircraft` entity) using the difference between the `manufacturingDate` and `LocalDate.now()`.

---

# 3. Design

## 3.1 Architectural Approach

The implementation follows the layered architecture adopted in the project:

Presentation -> Application -> Domain -> Infrastructure


Responsibilities:

| Layer          | Responsibility                        |
| -------------- | ------------------------------------- |
| Presentation   | User interaction and menu rendering   |
| Application    | Use case coordination and sorting     |
| Domain         | Business rules, entities and age math |
| Infrastructure | Persistence and session tracking      |

---

## 3.2 Main Components

### Presentation Layer

Package:

pt.isep.sem4pi.presentation.airtransportcompany


Main component:

* `ListCompanyFleetUI`

Responsibilities:

* Ask the user for the preferred sorting option;
* Display the returned list of aircraft in a formatted table;
* Present error messages when applicable.

---

### Application Layer

Package:

pt.isep.sem4pi.application.controller.airtransportcompany


Main component:

* `ListCompanyFleetController`
* `FleetSortOption` (Enum)

Responsibilities:

* Enforce authorization;
* Retrieve the current user's session;
* Resolve the Air Transport Company via the collaborator profile;
* Retrieve the fleet from the company aggregate;
* Sort the list based on the chosen `FleetSortOption`;
* Return the ordered list to the UI.

---

### Domain Layer

Packages:

pt.isep.sem4pi.domain.model.airtransportcompany
pt.isep.sem4pi.domain.model.aircraft
pt.isep.sem4pi.domain.repositories


Main components referenced:

* `AirTransportCompanyCollaborator` — entity linking the user to the company;
* `AirTransportCompany` — aggregate root that contains the fleet;
* `Aircraft` — entity representing the aircraft, responsible for calculating its own `age()`;
* `CabinConfiguration` — value object providing the `totalCapacity()`;
* `AirTransportCompanyCollaboratorRepository` — provides `findBySystemUser`.

---

## 3.3 Listing Flow

ListCompanyFleetUI
-> ListCompanyFleetController
-> AuthorizationService (authorization check and session retrieval)
-> AirTransportCompanyCollaboratorRepository (find profile by session user)
-> AirTransportCompany (getFleet())
-> Controller (sorts list via Java Comparator)
-> List (returned to UI)


---

## 3.4 Design Decisions

### In-Memory Sorting
Instead of creating 5 different JPQL queries in the repository layer to sort the fleet, the sorting is executed in-memory using Java's `Comparator`. Since the fleet belongs to the aggregate root, retrieving the aggregate already loads the fleet. Sorting it in memory avoids redundant database hits and keeps the repository interfaces clean.

### Aggregate Information Retrieval
Following pure DDD concepts, the controller does not use an `AircraftRepository` to find aircraft by company. Instead, it asks the `AirTransportCompany` aggregate for its fleet (`company.getFleet()`).

### Dynamic Age Calculation
Storing "age" as a persistent attribute is an anti-pattern (it becomes outdated the following year). Instead, `manufacturingDate` was added to the `Aircraft` entity, and a method `age()` dynamically calculates the years between the manufacturing date and the current date.

---

# 4. Architecture

## 4.1 Query Flow Summary

The operation is strictly read-only. No aggregate is modified. The controller reads the user session, navigates the domain hierarchy to fetch the fleet, and applies sorting algorithms before returning the result.

---

## 4.2 Repository Methods Used

| Repository                                  | Method                         | Purpose                                  |
| ------------------------------------------- | ------------------------------ | ---------------------------------------- |
| `AirTransportCompanyCollaboratorRepository` | `findBySystemUser(systemUser)` | Resolve the collaborator's company       |

*(Note: No Aircraft Repository is needed due to the DDD aggregate structure).*

---

# 5. Tests and Implementation

The implementation includes comprehensive testing aiming for 100% method coverage:

* Successful retrieval and sorting by Registration;
* Successful retrieval and sorting by Maker (Manufacturer);
* Successful retrieval and sorting by Model;
* Successful retrieval and sorting by Capacity;
* Successful retrieval and sorting by Age;
* Rejection when the user has no collaborator profile (`IllegalStateException`);
* Full enum coverage test;
* Default constructor initialization testing using an in-memory repository context.

Tests utilize:

* JUnit 4;
* Mockito (to mock User Sessions, Repositories, and Aircraft behavior);
* In-memory configuration blocks.

---

# 6. Conclusion

The implementation successfully fulfills US072, allowing an Air Transport Company Collaborator to list their specific fleet sorted by multiple criteria. The solution adheres strictly to Domain-Driven Design principles by relying on Aggregate roots for data fetching and isolating dynamic business rules (like age calculation) inside the Domain Entities.

---