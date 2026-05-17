# US-055: Add Aircraft Model

## 1. Context
Implementation of the functionality that allows a `BACKOFFICE_OPERATOR` to register a new Aircraft Model in the SEM4PI system. Each Aircraft Model groups its physical characteristics, wing geometry, performance metrics, fuel capacity, and one or more **certified** engine models. The combination of *manufacturer + model name* must be unique, and at least one Engine Model must be associated upon creation.

## 2. Architecture / Organization
- `domain.model.aircraftModel`: `AircraftModel`, `AircraftModelId`, `AircraftType`, `MotorizationType`, `Weight`, `WingGeometry`, `Fuel`, `AircraftPerformance`, `Coefficient`.
- `domain.model.engineModel`: `CertifiedEngine` (entity owned by `AircraftModel`, holding a reference to an `EngineModel`).
- `application`: `AddAircraftModelController`.
- `infrastructure.bootstrap`: `BackofficeConfigurationBootstrapper` (integrated bootstrap of Manufacturers, Engine Models, Aircraft Models and Air Control Areas).
- `infrastructure.persistence`: `InMemoryAircraftModelRepository` and `JpaAircraftModelRepository`.
- `presentation.aircraftModel`: `AddAircraftModelUI`, `AddAircraftModelAction`.
- `presentation`: `MainMenu`.

## 3. Created files and core responsibilities

- `src/main/java/pt/isep/sem4pi/domain/model/aircraftModel/AircraftModel.java`
  - The aggregate root for the Aircraft Model. Groups all value objects (`Weight`, `Fuel`, `AircraftPerformance`, `WingGeometry`, `Coefficient`) and owns a collection of `CertifiedEngine`. Enforces invariants: at least one engine, no duplicate engines, and engine-motorization compatibility.

- `src/main/java/pt/isep/sem4pi/domain/model/aircraftModel/AircraftModelId.java`
  - Composite identifier (`ManufacturerId` + `modelName`), making the *manufacturer + model name* uniqueness invariant explicit in the domain.

- `src/main/java/pt/isep/sem4pi/domain/model/aircraftModel/* (Value Objects)`
  - `Weight`, `AircraftPerformance`, `WingGeometry`, `Fuel`, `Coefficient` validate domain rules (e.g., rejecting negative or zero values where inappropriate) using `BigDecimal` to ensure precision in physical calculations. `AircraftType` and `MotorizationType` are implemented as Enums.

- `src/main/java/pt/isep/sem4pi/domain/model/engineModel/CertifiedEngine.java`
  - Represents a certification link between an `AircraftModel` and an `EngineModel`. Lives inside the `AircraftModel` aggregate. The aggregate root is the only point of entry for adding new certified engines.

- `src/main/java/pt/isep/sem4pi/application/AddAircraftModelController.java`
  - Orchestrates the use case. Receives raw data from the UI, enforces authorization, checks for the manufacturer+name uniqueness rule via the repository, constructs the Value Objects, builds the `AircraftModel` (which internally certifies the provided engines), and persists the new entity.

- `src/main/java/pt/isep/sem4pi/infrastructure/persistence/memory/InMemoryAircraftModelRepository.java`
  - In-memory persistence implementation. Implements lookup methods using Java Streams to iterate over saved instances.

- `src/main/java/pt/isep/sem4pi/infrastructure/persistence/JPA/JpaAircraftModelRepository.java`
  - JPA-backed persistence implementation, mapping the composite ID and the cascading `CertifiedEngine` collection.

- `src/main/java/pt/isep/sem4pi/infrastructure/bootstrap/BackofficeConfigurationBootstrapper.java`
  - Integrated bootstrap. Registers seed Manufacturers (`CFM`, `RR`, `GE`, `AIRBUS`), Engine Models (`CFM56-5B`, `TRENT1000`, `GENX-1B`), and an Aircraft Model (`A320` with `CFM56-5B` certified).

- `src/main/java/pt/isep/sem4pi/presentation/aircraftModel/AddAircraftModelUI.java` / `AddAircraftModelAction.java`
  - UI layer that interacts with the user. Utilizes EAPLI's `SelectWidget` to provide error-proof selections for Manufacturers, Aircraft Types, Motorizations, and Engine Models. Catches `IllegalArgumentException` from the domain to prevent application crashes on invalid inputs.

- `src/test/java/pt/isep/sem4pi/application/controller/aircraftModel/AddAircraftModelControllerTests.java`
  - JUnit 4 test class. Uses real `InMemory` repositories to accurately test business rules (such as duplication prevention and the at-least-one-engine invariant) and uses Mockito to mock the `AuthorizationService` and the `Manufacturer` reference.

- `src/test/java/pt/isep/sem4pi/domain/model/aircraftModel/AircraftModelTests.java`
  - Domain-level test class covering the aggregate's invariants: null parameter rejection, duplicate engine detection, identity-based equality, and acceptance of new compatible engines.

## 4. Application Flow
`MainMenu` (Operator Menu) → `AddAircraftModelAction.execute()` → `AddAircraftModelUI.doShow()` → Prompts user for selections via `SelectWidget` and manual `BigDecimal` inputs → Builds the engine list interactively → Calls `AddAircraftModelController.addAircraftModel(...)` → Controller validates authorization → Controller checks `aircraftRepo` for duplicate manufacturer+name → Instantiates Value Objects → Builds `AircraftModel`, which internally certifies the selected engines → `aircraftRepo.save()` → Returns success message to UI.

## 5. Domain Rules & Validation
- **Data Integrity:** All physical attributes are encapsulated in Value Objects that reject invalid values (e.g., null, negative or otherwise out-of-bounds values) via EAPLI's `Preconditions`.
- **At Least One Engine:** The `AircraftModel` constructor refuses an empty or null engine list.
- **No Duplicate Engines:** `certifyEngine()` rejects engines whose `EngineModelId` is already certified for the model.
- **Manufacturer + Name Uniqueness:** The controller verifies that no aircraft model with the same `(manufacturerId, modelName)` already exists before saving.
- **Engine Type Compatibility:** An engine can only be certified for an aircraft model whose `MotorizationType` matches the engine's `EngineType`.

## 6. Authorization
- Protected by `SEM4PIRoles.BACKOFFICE_OPERATOR`.
- `AddAircraftModelController` uses `authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR)` to guarantee that only authorized staff can execute the use case. Authorization is also enforced on the listing helpers (`getManufacturers`, `getEngineModels`).

## 7. Persistence
- **JPA** (production): `JpaAircraftModelRepository` persists the aggregate including its `CertifiedEngine` children via cascading composite-key joins.
- **In-Memory** (alternative, NFR08): `InMemoryAircraftModelRepository` extends EAPLI's `InMemoryDomainRepository` and serves as a drop-in replacement when running with the in-memory profile.
- The `BackofficeConfigurationBootstrapper` seeds the database/in-memory store so that the use case can be exercised end-to-end immediately after startup.

## 8. Testing Strategy
- **Controller tests** use real in-memory repositories (`InMemoryAircraftModelRepository`, `InMemoryManufacturerRepository`, `InMemoryEngineModelRepository`) rather than mocking them, ensuring that the actual repository logic participates in the test. Mockito is reserved for the `AuthorizationService` and the `Manufacturer` reference.
- **Domain tests** focus on the aggregate invariants: null guards on the constructor, duplicate engine rejection, engine compatibility, identity-based `sameAs`, and identity retrieval.
- This hybrid approach validates both the orchestration in the application layer and the integrity rules in the domain layer.

## 9. Manual testing steps (Quick)
- Log in as `backoffice` / `Password1` (or `bo` / `Password1`, depending on the bootstrapped credentials).
- Navigate to `Operator >` → `Add New Aircraft Model`.
- Select a Manufacturer (e.g., `AIRBUS`) and an Aircraft Type (e.g., `PASSENGER`) using the numbered menus.
- Choose a Motorization (e.g., `TURBOFAN`).
- Input valid numbers for the requested specifications (weights, performance, geometry, coefficients).
- Select at least one compatible Engine Model from the list (e.g., `CFM56-5B` for a `TURBOFAN` aircraft).
- Verify the success message.
- Repeat the process selecting the **same** Manufacturer and typing the **same** Model Name to trigger and observe the domain duplication error gracefully handled by the UI.
- Optionally, try selecting an incompatible engine type to verify the compatibility check.

## 10. Conclusion
US055 is fully implemented as a complete vertical slice. It leverages the EAPLI framework's UI widgets for a robust user experience, strictly follows Domain-Driven Design principles by keeping `CertifiedEngine` inside the `AircraftModel` aggregate and referencing the external `EngineModel` aggregate only by identity through this owned entity. Business rules — uniqueness, at-least-one-engine, no duplicate engines, and engine-motorization compatibility — are enforced at the domain layer and verified by both domain-level and application-level tests. The bootstrap process registers the required seed data so that the use case is immediately exercisable.