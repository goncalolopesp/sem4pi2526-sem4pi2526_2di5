# Aggregate Design Justification

## 1. Introduction

This document justifies the aggregate design decisions derived from the domain model of the AISafe system. Given the complexity of the domain-covering identity management, air transport operations, flight planning, weather data, and simulation. 
The system was decomposed into multiple aggregates to ensure clear consistency boundaries, scalability, and alignment with Domain-Driven Design (DDD) principles.

The aggregates reflect the system’s core responsibilities, as defined in the project requirements, particularly:

* Backoffice configuration of core data (aircraft, airports, etc.)
* Flight management and planning
* Weather data ingestion and consultation
* Flight simulation and safety validation

---

## 2. Design Principles Applied

### 2.1 Consistency Boundaries

Aggregates were defined around invariants that must be strongly consistent (e.g., a FlightPlan must always have valid flights and fuel information).

### 2.2 High Cohesion

Entities that participate in the same business rules or lifecycle were grouped together.

### 2.3 Low Coupling

Cross-aggregate relationships are maintained through identifiers (e.g., FlightPlan references Aircraft, Pilot, and FlightRoute).

### 2.4 Scalability and Performance

Given the requirement to support **many simultaneous simulations and flights**, aggregates were kept small to allow parallel processing and distribution .

---

## 3. Aggregate Justifications

---

### 3.1 User Aggregate

**Aggregate Root:** User

**Entities / Value Objects:**

* Email (VO)
* PhoneNumber (VO)

**Justification:**
The User aggregate encapsulates authentication and identity concerns, which are central to system access (US030-US033). Each user must maintain consistent personal data, status, and security constraints (e.g., clearance expiration and skills assessment).

**Invariant Example:**

* A user must always have valid email and phone number.
* A user must have a valid security clearance to interact with the system.

**Why separate from Role?**
Roles are modeled as a separate aggregate to allow flexibility (future support for multiple roles per user), as explicitly mentioned in the requirements.

---

### 3.2 Role Aggregate

**Aggregate Root:** Role

**Justification:**
Roles are relatively static and define authorization rules. Separating them avoids coupling identity with authorization policies, supporting extensibility (new roles may be added in the future).

**Invariant Example:**

* Role names must be unique.

---

### 3.3 Manufacturer Aggregate

**Aggregate Root:** Manufacturer

**Justification:**
Manufacturers are simple reference data with minimal lifecycle and no strong invariants with other aggregates. They are independent because they are not system clients and only provide static data (requirement section 3.1.2).

---

### 3.4 EngineModel Aggregate

**Aggregate Root:** EngineModel

**Justification:**
Engine models have their own identity and lifecycle and are reused across multiple aircraft models. Keeping them separate avoids duplication and supports reuse.

**Invariant Example:**

* Engine model name + manufacturer must be unique (US056).

---

### 3.5 AircraftModel Aggregate

**Aggregate Root:** AircraftModel

**Justification:**
Aircraft models encapsulate complex invariants related to flight physics (weight, drag, lift, etc.) and certified engine models.

**Invariant Example:**

* Must have at least one certified engine model (US055).
* Engine compatibility must be enforced (US057).

---

### 3.6 Airport Aggregate

**Aggregate Root:** Airport

**Value Objects:**

* Coordinates

**Justification:**
Airports are core domain entities used across multiple contexts (routes, flights, simulations). They must enforce uniqueness of IATA/ICAO codes.

**Invariant Example:**

* IATA and ICAO codes must be globally unique (US052).

---

### 3.7 AirControlArea Aggregate

**Aggregate Root:** AirControlArea

**Value Objects:**

* GeographicBoundary

**Justification:**
Air control areas define operational constraints (e.g., minimum reserve fuel rules) and are essential for simulation and validation.

**Invariant Example:**

* Geographic boundary must be valid.
* Minimum fuel rule must be defined.

---

### 3.8 AirTransportCompany Aggregate

**Aggregate Root:** AirTransportCompany

**Entities:**

* CompanyCollaborator
* Pilot
* Aircraft

**Value Objects:**

* CabinConfiguration

**Justification:**
This aggregate groups all operational resources of a company:

* Fleet (Aircraft)
* Human resources (Pilots, Collaborators)
* Managed routes

This reflects the requirement that companies manage aircraft, pilots, and routes (section 3.1.3).

**Invariant Example:**

* Aircraft must belong to exactly one company.
* Pilot cannot be deactivated if assigned to flight plans (US077).
* Aircraft seat configuration must respect model constraints (US070).

---

### 3.9 FlightRoute Aggregate

**Aggregate Root:** FlightRoute

**Justification:**
Flight routes represent reusable definitions between two airports. They have their own lifecycle (activation/deactivation).

**Invariant Example:**

* Route code must be unique (US073).
* Cannot be deactivated if future flights depend on it (US074).

---

### 3.10 FlightPlan Aggregate

**Aggregate Root:** FlightPlan

**Entities:**

* Flight

**Value Objects:**

* RouteDefinition
* FuelInfo
* AltitudeSlot

**Justification:**
This is a core aggregate representing the planning phase of flights. It encapsulates:

* DSL-based structure
* Validation rules
* Fuel and route constraints

It aligns directly with the requirement that **a flight must have a valid flight plan to take place** .

**Invariant Example:**

* A flight plan must contain at least one flight.
* Fuel must be positive and sufficient.
* Route must match origin/destination.
* DSL must be valid (syntactic and semantic validation).

---

### 3.11 WeatherSource Aggregate

**Aggregate Root:** WeatherSource

**Justification:**
Represents external providers of weather data. It is independent to allow extensibility (multiple providers supported).

---

### 3.12 WeatherImportBatch Aggregate

**Aggregate Root:** WeatherImportBatch

**Entities:**

* WeatherData

**Justification:**
Weather data is imported in batches, and consistency must be ensured within each batch.

**Invariant Example:**

* All imported data must be associated with a source.
* Batch status must reflect processing result (US042).

---

### 3.13 Simulation Aggregate

**Aggregate Root:** Simulation

**Value Objects:**

* SimulatedFlightInput

**Justification:**
Simulation represents the request and lifecycle of a simulation executed by
the external SCOMP component. It keeps the selected flight-plan identifiers,
area, time interval, safety parameters and execution status.

The detailed flight evolution, position tracking and safety detection remain
inside SCOMP. The Java aggregate therefore stays small and does not duplicate
the simulation engine state.

**Invariant Examples:**

* A simulation must contain valid execution parameters.
* Only a created simulation may start.
* Only a running simulation may finish.

---

### 3.14 SimulationReport Aggregate

**Aggregate Root:** SimulationReport

**Value Objects:**

* FlightExecutionSummary
* SafetyViolationSummary
* ReportFilePath
* SimulationValidationResult

**Justification:**
SimulationReport has its own identity, generation timestamp, file location and
repository lifecycle. It summarizes the final SCOMP/US109 result for EAPLI and
supports US111 without reproducing the simulation algorithm.

Flight and violation summaries have no independent identity or lifecycle. They
are immutable descriptive values owned by the report.

**Invariant Examples:**

* The total number of flights equals the number of flight summaries.
* A SCOMP failure produces `FAIL`.
* Any unfinished flight produces `FAIL`.
* Any safety violation produces `FAIL`.
* `PASS` requires a successful SCOMP result, all flights finished and no safety
  violations.

---

## 4. Communication Between Aggregates

Aggregates interact via:

* Identifiers (e.g., FlightPlan → Aircraft, Pilot)
* Domain events (e.g., simulation events, weather updates)
* Application services coordinating workflows

This is essential to support:

* Distributed simulation
* Remote access (TCP clients)
* Integration with DSL processing and external systems

---

## 5. Trade-offs

### Advantages:

* Strong alignment with business capabilities (planning, simulation, configuration)
* High scalability for simulation (parallel processes)
* Clear separation between static data and operational data

### Disadvantages:

* Increased complexity in cross-aggregate coordination
* Need for eventual consistency (e.g., between FlightPlan and Simulation)
* More complex application services

---

## 6. Conclusion

The aggregate design reflects the natural boundaries of the AISafe domain:

* Configuration (Aircraft, Airports, Areas)
* Operations (Companies, Routes)
* Planning (Flight Plans)
* Execution (Simulation)
* External data (Weather)

By isolating invariants and minimizing coupling, the system supports both **complex validation logic** and **high-performance simulation requirements**, as demanded by the project.

---
