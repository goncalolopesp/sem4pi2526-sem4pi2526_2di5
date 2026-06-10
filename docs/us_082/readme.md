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
- A pilot can only update their own flight plans.
- Weather data must exist in the system before it can be associated.
- A flight plan can have zero, one or multiple weather records.
- Weather data is not required when creating a flight plan in US080.
- All relevant weather measurements are displayed before selection.
- One or multiple weather records can be selected in a single operation.
- Exactly identical weather records are consolidated in the selection list.
- Weather data already associated with the flight plan is not duplicated.
- If the flight plan was previously tested, its previous result is voided.
- The system informs the user when the previous test is no longer valid.

### 1.5. Definition of Done

- Optional weather data association implemented.
- Multiple weather data association implemented.
- Pilot ownership validation implemented.
- Administrator access implemented.
- Previous test invalidation rule implemented.
- Complete weather data presentation implemented.
- Duplicate prevention implemented.
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

Weather data is stored inside `WeatherImportBatch` aggregates. Each persisted
`WeatherData` entity has its own identifier. The identifier is used when
associating records so that different measurements with the same area, date
and time interval are not confused.

For presentation purposes, records are considered identical when all of the
following values match:

- Air Control Area;
- date;
- start and end time;
- temperature;
- wind speed and direction;
- atmospheric pressure;
- humidity;
- altitude.

Exactly identical records are consolidated into one row. Records with
different measurements remain separate choices even when their area and time
interval are equal.

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
5. The system lists existing weather records and all their measurements.
6. Exactly identical records are consolidated into one selection row.
7. The user selects one or more rows using comma-separated numbers, such as
   `1,3,5`.
8. `AssociateWeatherDataService` retrieves the flight plan and validates
   ownership, unless the user is an administrator.
9. Each selected record is retrieved by its persisted identifier.
10. `FlightPlan.associateWeatherData()` adds each new record to the collection.
11. Repeated selections and records already associated are ignored.
12. If the flight plan had already been tested, its status returns to `DRAFT`.
13. The flight plan is persisted once after all records are processed.
14. The UI reports how many records were associated and warns the user when
    the previous test is no longer valid.

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

- If the user has neither the `PILOT` nor the `ADMIN` role, access is denied.
- If the pilot has no flight plans, the UI informs the user.
- If no weather data exists, the UI informs the user.
- If no weather row is selected, the request is rejected.
- If a row number is invalid, the UI reports the invalid selection.
- If the selected flight plan does not exist, the request is rejected.
- If a pilot selects another pilot's flight plan, the request is rejected.
- If selected weather data no longer exists, the request is rejected.
- If the same row is entered more than once, it is processed only once.
- If weather data is already associated, it is not added again.
- If the flight plan is in `DRAFT`, no previous-test warning is displayed.

## 4. Implementation

### 4.1. Main Components

- `AssociateWeatherDataUI` presents flight plans and complete weather data,
  accepts multiple row numbers and reports the result.
- `AssociateWeatherDataController` applies authorization and obtains the
  authenticated username.
- `AssociateWeatherDataService` validates ownership, consolidates identical
  records, resolves persisted identifiers and coordinates the batch update.
- `WeatherAssociationResult` carries the updated flight plan, the invalidation
  result and the number of records associated.
- `FlightPlan` owns the optional collection of associated `WeatherData`.
- `WeatherBatchRepository` lists weather data and retrieves records by ID.
- `FlightPlanRepository` retrieves and persists the updated aggregate.

### 4.2. Persistence

The association is persisted through the join table:

```text
flight_plan_weather_data
```

The table links `FlightPlan` and `WeatherData` without making weather data
mandatory.

`InMemoryWeatherBatchRepository` and `JpaWeatherBatchRepository` support
retrieving a weather record by its persisted identifier. Existing area, date
and time lookups remain available for other use cases.

### 4.3. Weather Presentation and Selection

The console table displays:

- Air Control Area;
- date;
- start and end time;
- temperature in degrees Celsius;
- wind speed in knots;
- wind direction in degrees;
- atmospheric pressure in hPa;
- humidity percentage;
- altitude in metres.

Multiple records are selected in one input:

```text
Weather rows (comma-separated, e.g. 1,3,5): 1,3,5
```

The selected rows are converted into persisted `WeatherData` identifiers.
Repeated row numbers are removed and the flight plan is saved once after all
new records have been associated.

### 4.4. Test Invalidation

`FlightPlan.associateWeatherData()` indicates whether the previous test became
invalid:

- `false` when the flight plan was already in `DRAFT`;
- `true` when its previous status was `VALIDATED` or `REJECTED`.

When invalidation occurs, the UI displays:

```text
[WARNING] The previous flight plan test is no longer valid.
```

## 5. Tests

### 5.1. Domain Tests

- `FlightPlanTest` verifies that a flight plan works without weather data.
- It verifies that multiple weather records can be associated.
- It verifies that duplicate associations are not created.
- It verifies that adding weather data to a validated plan returns it to
  `DRAFT`.
- It verifies that adding weather data to a draft plan does not report
  invalidation.
- `PilotTest` verifies username-based ownership.

### 5.2. Application Service Tests

- `AssociateWeatherDataServiceTest` verifies association and persistence.
- It verifies ownership restrictions and administrator access rules.
- It verifies that weather data must already exist.
- It verifies that multiple persisted weather identifiers are processed in a
  single operation.
- It verifies that repeated identifiers are processed only once.
- It verifies that the flight plan is persisted once after batch association.
- It verifies the number of newly associated records and invalidation result.

### 5.3. Bootstrap Test

- `FlightPlanBootstrapperTest` verifies that `FP-DEMO-001` is created without
  weather data and can be used to test this use case.
- It verifies that running the bootstrap repeatedly does not duplicate the
  flight plan.

### 5.4. Main Test Scenarios

| Scenario | Expected result |
|---|---|
| Create a flight plan without weather data | Flight plan is created normally by US080 |
| Add one existing weather record | Weather data is associated |
| Select `1,3,5` | All three distinct records are associated in one operation |
| Same area and time with different measurements | Each record appears separately with complete values |
| Several records are exactly identical | Only one selection row is displayed |
| Select the same row more than once | The record is processed only once |
| Select weather already associated | Duplicate association is not created |
| Add weather to a previously tested plan | Status returns to `DRAFT` and a warning is displayed |
| Enter an invalid row number | Selection is rejected with a clear error |
| Select weather that no longer exists | Request is rejected |
| Pilot selects another pilot's plan | Request is rejected |
| Administrator selects any flight plan | Weather data can be associated |
| Unauthorized role | Access is denied before service execution |
