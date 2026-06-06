# US080 - Create a flight plan

## User Story

As Pilot, I want to register a flight plan for a route. I must add the aircraft, departure date/time, fuel quantity, and pilot. The pilot must belong to the route's company. Flight plan status is set to **DRAFT** when created and must undergo a multi-step validation process.

## Implementation

- **Domain:** `FlightPlan` aggregate with `FlightLeg`, fuel, departure/arrival value objects, and `FlightPlanStatus` (`DRAFT`, `VALIDATED`, `REJECTED`).
- **Application:** `CreateFlightPlanService` and `CreateFlightPlanController` (role `PILOT` or `ADMIN`).
- **Presentation:** `CreateFlightPlanUI` under menu **Flight Planning > Create flight plan**.
- **Bootstrap:** user `pilot` / `Password1` and sample pilot `PILOT01` for company `TP`.

## Validations

- Route must exist.
- At least one leg; first/last leg airports must match route origin/destination.
- Fuel quantity strictly positive.
- Pilot must belong to the route company.
- Aircraft must belong to the company fleet and cannot be retired.
- Flight plan id must be unique.
- New plans start in `DRAFT` status (`validate()` / `reject()` support later steps).

## Related stories

- **US081:** import plan from DSL file.
- **US085:** test/validate flight plan (C component).
