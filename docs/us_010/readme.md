# US 010

## 1. Context

"As Project Manager, I want the team to elaborate a Domain Model using DDD.
Because AlSafe domain is complex, involving distinct areas and complex business rules.
We will adopt DDD as a framework for tackling complexity in our software solution, and ensure the software is a valid implementation of the business needs."

### 1.1 List of issues

**Analysis:** Identify all concepts, entities, and business rules of the AlSafe system from the client specifications.

**Design:** Draw the conceptual class diagram (Domain Model) grouping entities into logical Aggregates and defining Aggregate Roots, Entities, and Value Objects based on the distinct domain areas.

**Implement:** Map the designed domain model to Java classes.

**Test:** Ensure business rules reflected in the domain are covered by unit tests with a minimum 90% coverage.

## 2. Requirements

**US010** As Project Manager, I want the team to elaborate a Domain Model using DDD.

**Acceptance Criteria:**

- AC 1 The model must support and reflect the complexity of the AlSafe domain, including distinct areas and complex business rules.
- AC 2 It must use the DDD approach to ensure the software is a valid implementation of the business needs.

**Dependencies/References:**

This US is the foundation of the project. It relates directly to US011, where the team must illustrate one representative scenario per aggregate with a sequence diagram and a short explanation to demonstrate the aggregate's responsibility.

## 3. Analysis

The AlSafe system focuses on air traffic control and simulation. The domain was organized into six main bounded contexts/packages:

* **Identity & Access:** Manages authentication and roles. 
  * Key concepts include `User` and `Role`.

* **Backoffice Configuration:** Catalog entities managed by the backoffice.
  * Includes `AirControlArea`, `Airport`, `Manufacturer`, `AircraftModel`, and `EngineModel`.

* **Air Transport Operations:** Managed by air transport companies. 
  * Encompasses the `AirTransportCompany`, its `FlightRoute`, the `Aircraft` fleet, `CustomerCollaborator`, and `Pilot`.
  
* **Flight Planning:** The formal operation planning. 
  * Involves the `FlightPlan` (which includes `FuelInfo` and `RouteDefinition`) and the specific `Flight` instantiations.
  
* **Weather Service:** Weather data introduced in the system that affects the simulation. 
  * Includes `WeatherImportBatch`, `WeatherSource`, and `WeatherData`.
  
* **Flight Simulation:** Integration with the external simulation component.
  * Involves `Simulation`, `SimulatedFlightInput` and the `SimulationReport`
    summary received from the external SCOMP component.

* **Reporting:** Defines the common structure and persisted metadata of
  generated reports.
  * Includes `Report`, `ReportSection`, `MonthlyStatisticsReport` and
    `ReportMetadata`.

## 4. Design

Domain-Driven Design concepts were used. Entities with independent lifecycles were separated into distinct aggregates, referencing each other through unique identifiers (IDs) instead of direct object references to reduce coupling.

Main identified Aggregate Roots (`<<AggregateRoot>>`):
* `User`
* `Role`
* `AirTransportCompany`
* `CustomerCollaborator`
* `Pilot`
* `Aircraft`
* `FlightRoute`
* `Flight`
* `Airport`
* `AirControlArea`
* `Manufacturer`
* `AircraftModel`
* `EngineModel`
* `FlightPlan`
* `Simulation`
* `SimulationReport`
* `WeatherImportBatch`
* `WeatherSource`
* `WeatherData`
* `ReportMetadata`

### 4.1. Realization

![domain-model.svg](DM-aggregate.svg "AlSafe Domain Model Class Diagram")

### 4.2. Acceptance Tests

N/A
