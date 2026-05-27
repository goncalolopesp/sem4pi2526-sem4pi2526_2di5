# US043 - Consult weather data

## 1. Requirements Engineering

### 1.1. User Story Description
As a Weather Person, a Pilot, or a Flight Control operator, I want to consult weather data in the system in a given day and in a specific air control area.

### 1.2. Context & Constraints
Weather data is consulted to support flight validation, simulation, and environmental influence calculations. The feature must be protected by role-based access control.

### 1.3. Dependencies
* US041 - Register weather data
* US042 - Import bulk weather data
* US050 - Register an air control area
* US030 - Authentication and authorization

## 2. Analysis & Design

### 2.1. Use Case Flow
1. User provides air control area ID and a date.
2. System filters weather batches by area ID and records by date.
3. System returns matching records in a clear, tabular view.
4. If there are no matches, the system informs the user.

### 2.2. Authorization
Access is restricted to users with roles `ADMIN`, `WEATHER_PERSON`, `PILOT`, or `FLIGHT_CONTROL_OPERATOR`.

## 3. Implementation

### 3.1. Main Components
* `ConsultWeatherDataController` validates authorization and delegates to the service.
* `ConsultWeatherDataService` filters imported batches and maps records to DTOs.
* `ConsultWeatherDataUI` collects filters and renders results.

## 4. Testing

* `ConsultWeatherDataServiceTest` ensures filtering by area and date returns expected results.

