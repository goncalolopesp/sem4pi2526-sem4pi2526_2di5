# US082 - Add Weather Data to a Flight Plan

## 1. Requirements Engineering

### 1.1. User Story

As a Pilot, I want to add weather data to a flight plan of mine. If the flight
plan has been previously tested, the test is deemed void because of the new
weather data.

### 1.2. Context

Weather conditions can affect flight plan validation and simulation results.
When weather data is added after a flight plan has been tested, the previous
result can no longer be trusted and the flight plan must be tested again.

This user story extends US080 without changing its creation flow. Weather data
is optional: a flight plan can be created and used without any associated
weather records.

### 1.3. Dependencies

- **US030:** Authentication and authorization.
- **US041:** Register weather data.
- **US042:** Import bulk weather data.
- **US043:** Consult weather data.
- **US080:** Create a flight plan.

### 1.4. Acceptance Criteria

- A pilot can add weather data to one of their flight plans.
- An administrator can add weather data to any flight plan.
- Weather data must exist in the system before it can be associated.
- A pilot can only update their own flight plans.
- A flight plan can have zero, one or multiple weather records.
- Weather data is not required when creating a flight plan in US080.
- If the flight plan was previously tested, its previous result is voided.
- The system informs the user when the previous test is no longer valid.

### 1.5. Definition of Done

- Weather data association implemented.
- Optional `FlightPlan` to `WeatherData` relationship implemented.
- Pilot ownership validation implemented.
- Administrator access implemented.
- Previous test invalidation rule implemented.
- Unit tests created.
- Documentation updated.
- CI passes successfully.

## 2. Analysis

`FlightPlan` is the aggregate root responsible for maintaining its associated
weather data. The relationship is optional and supports multiple records:

```text
FlightPlan 0..* WeatherData
```

The `weatherData` collection is initialized as empty. Consequently, US080 does
not need to provide weather data to construct, persist, validate or otherwise
use a flight plan.

Weather data is stored inside `WeatherImportBatch` aggregates. A record is
identified for this use case by:

- Air Control Area identifier;
- date;
- start time;
- end time.

The application service only associates a record returned by
`WeatherBatchRepository`. This prevents weather data that does not exist in the
system from being added to a flight plan.

Ownership is determined through the username assigned to the `Pilot` entity:

- a `PILOT` may access and update only flight plans owned by that username;
- an `ADMIN` may access and update any flight plan.

When weather data is added to a flight plan whose status is `VALIDATED` or
`REJECTED`, the status returns to `DRAFT`. This represents the invalidation of
the previous test and requires the flight plan to be tested again.

## 3. Design

### 3.1. Main Flow

1. The user selects **Add weather data to flight plan**.
2. `AssociateWeatherDataController` verifies that the authenticated user has
   the `PILOT` or `ADMIN` role.
3. The system lists the flight plans accessible to the current user.
4. The user selects a flight plan.
5. The system lists weather records that already exist in the system.
6. The user selects a weather record.
7. `AssociateWeatherDataService` retrieves the flight plan and validates
   ownership, unless the current user is an administrator.
8. The service retrieves the selected weather record through
   `WeatherBatchRepository`.
9. `FlightPlan.associateWeatherData()` adds the record to the collection.
10. If the flight plan had already been tested, its status returns to `DRAFT`.
11. The updated flight plan is persisted.
12. The UI confirms the association and warns the user when the previous test
    is no longer valid.

### 3.2. Authorization and Ownership

Access is allowed to:

- `PILOT`;
- `ADMIN`.

A pilot only sees and updates flight plans associated with their own `Pilot`
entity. An administrator sees and updates every flight plan.

Role authorization is applied by `AssociateWeatherDataController`. Ownership
validation is applied by `AssociateWeatherDataService` before the aggregate is
changed.

### 3.3. Alternative Flows

- If the authenticated user has neither the `PILOT` nor the `ADMIN` role,
  access is denied before the service is executed.
- If the pilot has no flight plans, the UI informs the user and ends the
  operation.
- If no weather data exists, the UI informs the user and no association is
  created.
- If the selected flight plan does not exist, the service rejects the request.
- If a pilot selects another pilot's flight plan, the service rejects the
  request.
- If the selected weather record does not exist, the service rejects the
  request.
- If the weather record is already associated, it is not added again.
- If the flight plan is still in `DRAFT`, weather data is added without
  reporting a previous test invalidation.

## 4. Implementation

### 4.1. Main Components

- `AssociateWeatherDataUI` displays accessible flight plans and existing
  weather records, collects the selections and reports the result.
- `AssociateWeatherDataController` applies role-based authorization and
  obtains the authenticated username.
- `AssociateWeatherDataService` validates ownership, retrieves persisted
  weather data and coordinates the update.
- `WeatherAssociationResult` carries the updated flight plan and indicates
  whether a previous test was invalidated.
- `FlightPlan` owns the optional collection of associated `WeatherData`.
- `WeatherBatchRepository` supports listing and locating existing weather
  records.
- `FlightPlanRepository` retrieves and persists the updated aggregate.

### 4.2. Persistence

The association is persisted through a join table:

```text
flight_plan_weather_data
```

The table links `FlightPlan` and `WeatherData` without making weather data
mandatory. Both `InMemoryWeatherBatchRepository` and
`JpaWeatherBatchRepository` support locating a record by area, date and time
interval.

### 4.3. Test Invalidation

`FlightPlan.associateWeatherData()` returns a boolean indicating whether the
previous test became invalid:

- `false` when the flight plan was already in `DRAFT`;
- `true` when its previous status was `VALIDATED` or `REJECTED`.

When the method returns `true`, the UI displays:

```text
[WARNING] The previous flight plan test is no longer valid.
```

## 5. Tests

### 5.1. Domain Tests

- `FlightPlanTest` verifies that a flight plan can exist and be validated
  without weather data.
- It verifies that multiple weather records can be associated.
- It verifies that adding weather data to a validated flight plan returns it
  to `DRAFT`.
- It verifies that adding weather data to a draft flight plan does not report
  a previous test invalidation.
- `PilotTest` verifies username-based ownership.

### 5.2. Application Service Tests

- `AssociateWeatherDataServiceTest` verifies that an existing weather record
  is associated and the flight plan is persisted.
- It verifies that the invalidation result is returned to the presentation
  layer.
- It verifies that a pilot cannot update another pilot's flight plan.
- It verifies that weather data must already exist in the repository.
- It verifies that only flight plans owned by the pilot are listed.

### 5.3. Main Test Scenarios

| Scenario | Expected result |
|---|---|
| Create a flight plan without weather data | Flight plan is created normally by US080 |
| Add existing weather data to an owned draft plan | Weather data is associated and the plan remains `DRAFT` |
| Add several existing weather records | All distinct records are associated |
| Add weather data to a previously tested plan | Status returns to `DRAFT` and a warning is displayed |
| Add the same weather record again | Duplicate association is not created |
| Select weather data that does not exist | Request is rejected |
| Pilot selects another pilot's flight plan | Request is rejected |
| Administrator selects any flight plan | Weather data can be associated |
| Unauthorized role | Access is denied before the service is executed |