# US-055: Add Aircraft Model

## 1. Context
Implementation of the functionality that allows a `BACKOFFICE_OPERATOR` to register a new Aircraft Model in the SEM4PI system. Each Aircraft Model groups its physical characteristics, wing geometry, performance metrics, fuel capacity, maximum passenger capacity, and one or more **certified** engine models. The combination of *manufacturer + model name* must be unique, and at least one Engine Model must be associated upon creation.

## 2. Architecture / Organization
- `domain.model.aircraftModel`: `AircraftModel`, `AircraftModelId`, `AircraftType`, `MotorizationType`, `Weight`, `WingGeometry`, `Fuel`, `AircraftPerformance`, `Coefficient`.
- `domain.model.aircraftModel.DTO`: `AircraftModelDTO` and related DTOs.
- `domain.model.engineModel`: `CertifiedEngine`.
- `application.service.aircraftModel`: `AddAircraftModelService`.
- `application.viadto`: `AircraftModelDTORepresentationBuilder`.
- `application.controller.aircraftModel`: `AddAircraftModelController`.
- `infrastructure.bootstrap`: `BackofficeConfigurationBootstrapper` (integrated bootstrap of Manufacturers, Engine Models, Aircraft Models and Air Control Areas).
- `infrastructure.persistence`: `InMemoryAircraftModelRepository` and `JpaAircraftModelRepository`.
- `presentation.aircraftModel`: `AddAircraftModelUI`.
- `presentation`: `MainMenu`.

## 3. Created files and core responsibilities

- `src/main/java/pt/isep/sem4pi/domain/model/aircraftModel/AircraftModel.java`
  - The aggregate root for the Aircraft Model. Groups all value objects and owns a collection of `CertifiedEngine`. Enforces invariants: at least one engine, no duplicate engines, maximum capacity > 0, and engine-motorization compatibility. **Strictly encapsulated:** Exposes no getters, implementing the EAPLI `Representationable` interface to build DTOs via a `RepresentationBuilder`.

- `src/main/java/pt/isep/sem4pi/domain/model/aircraftModel/AircraftModelId.java`
  - Composite identifier (`ManufacturerId` + `modelName`), making the *manufacturer + model name* uniqueness invariant explicit in the domain.

- `src/main/java/pt/isep/sem4pi/domain/model/aircraftModel/* (Value Objects)`
  - `Weight`, `AircraftPerformance`, `WingGeometry`, `Fuel`, `Coefficient` validate strict domain rules using `BigDecimal`. For example, `Weight` ensures `EmptyWeight < MZFW < MTOW`, and `Coefficient` ensures realistic drag/lift bounds.

- `src/main/java/pt/isep/sem4pi/application/service/aircraftModel/AddAircraftModelService.java`
  - **Domain Service:** Handles the core business logic. Validates uniqueness, fetches the attached `EngineModel` entities from the database (preventing Hibernate `LazyInitializationException` / Proxy errors), constructs the Value Objects, and builds the `AircraftModel`.

- `src/main/java/pt/isep/sem4pi/application/viadto/AircraftModelDTORepresentationBuilder.java`
  - Safely extracts data from the `AircraftModel` domain entity into an `AircraftModelDTO` without breaking encapsulation (no getters required).

- `src/main/java/pt/isep/sem4pi/application/controller/aircraftModel/AddAircraftModelController.java`
  - Orchestrates the use case. Receives raw data from the UI, delegates aggregate creation to the `AddAircraftModelService`, persists the entity via the repository, and utilizes the `AircraftModelDTORepresentationBuilder` to return a safe DTO to the presentation layer.

- `src/main/java/pt/isep/sem4pi/infrastructure/persistence/JPA/JpaAircraftModelRepository.java`
  - JPA-backed persistence implementation, mapping the composite ID and the cascading `CertifiedEngine` collection.

- `src/main/java/pt/isep/sem4pi/presentation/aircraftModel/AddAircraftModelUI.java`
  - UI layer that interacts with the user. Features **Fail-Fast UX**: preemptively filters compatible engines before selection and continuously validates progressive business rules (e.g., prompting the user immediately if MZFW is lower than Empty Weight) to prevent late-stage operation failures.

- `src/test/java/pt/isep/sem4pi/... (Tests)`
  - Covers Domain invariants (`AircraftModelTests`), Service logic via Mockito (`AddAircraftModelServiceTest`), Controller integration via In-Memory repositories (`AddAircraftModelControllerTests`), and Builder mapping logic (`AircraftModelDTORepresentationBuilderTest`).

## 4. Application Flow
`MainMenu` → `AddAircraftModelUI.doShow()` → UI prompts for basic data (Type, Motorization) → UI pre-filters compatible engines. If none exist, aborts immediately → UI safely prompts for numbers (Weight, Coefficient) enforcing progressive rules → UI prompts for `maximumCapacity` → User selects pre-filtered engines → Calls `Controller.addAircraftModel(...)` → Controller delegates to `Service.addAircraftModel(...)` → Service verifies authorization and uniqueness, fetches real `EngineModel` instances, and instantiates the `AircraftModel` → Controller saves to `AircraftRepo` → Controller builds `AircraftModelDTO` from the aggregate using the `RepresentationBuilder` → Attaches DB-generated Version to DTO → Returns DTO to UI → Displays success message.

## 5. Domain Rules & Validation
- **Data Integrity:** All physical attributes are encapsulated in Value Objects that reject invalid values via EAPLI's `Preconditions`.
- **Weight Progressive Rules:** `EmptyWeight` must be < `MZFW` (Maximum Zero Fuel Weight), which must be < `MTOW` (Maximum Take-Off Weight).
- **Aerodynamic Bounds:** Drag Coefficient must be `> 0` and `<= 1.0`. Lift Coefficient must be `> 0` and `<= 4.0`.
- **At Least One Engine:** The `AircraftModel` constructor refuses an empty or null engine list.
- **Engine Type Compatibility:** An engine can only be certified for an aircraft model whose `MotorizationType` matches the engine's `EngineType`.
- **Maximum Capacity:** The model must specify a positive `maximumCapacity` integer (seats).

## 6. Authorization
- Protected by `SEM4PIRoles.BACKOFFICE_OPERATOR` and `ADMIN`.
- Enforced at the `AddAircraftModelService` and Controller helpers ensuring that only authorized staff can execute the use case.

## 7. Persistence
- **JPA** (production): `JpaAircraftModelRepository` persists the aggregate including its `CertifiedEngine` children via cascading composite-key joins.
- Avoids `LazyInitializationException` by explicitly fetching active Engine DB sessions in the Service layer before aggregate construction.
- The `BackofficeConfigurationBootstrapper` seeds the database/in-memory store so that the use case can be exercised end-to-end immediately after startup.

## 8. Testing Strategy
- **Domain Tests (`AircraftModelTests`):** Focus on aggregate invariants (null guards, duplicate engine rejection, capacity bounds).
- **Service Tests (`AddAircraftModelServiceTest`):** Uses Mockito to isolate business logic, ensuring proper uniqueness validation and correct Engine DB fetching.
- **Controller Tests (`AddAircraftModelControllerTests`):** Uses EAPLI's real `InMemory` repositories for integration testing without mocking the persistence behavior.
- **DTO/Builder Tests (`AircraftModelDTORepresentationBuilderTest`):** Ensures accurate data extraction from the encapsulated aggregate.

## 9. Manual testing steps (Quick)
- Log in as `backoffice` / `Password1`.
- Navigate to `Backoffice Configuration >` → `Add New Aircraft Model`.
- Select a Manufacturer (e.g., `AIRBUS`) and an Aircraft Type.
- Choose a Motorization. **(Test Fail-Fast):** Choose `ELECTRIC_PROPELLER`. The UI should immediately abort stating no compatible engines exist.
- Restart and choose `TURBOFAN`.
- Input physical data. **(Test Validation):** Enter `10000` for Empty Weight, then `5000` for MZFW. The UI should immediately reject it and ask again.
- Enter valid Coefficient limits and `Maximum Capacity` (e.g., `180`).
- Select a compatible Engine Model (e.g., `CFM56-5B`).
- Verify the success message containing the Model Name and DB Version.

## 10. Conclusion
US055 is fully implemented as a complete vertical slice with strict adherence to Domain-Driven Design (DDD). It successfully hides all internal state (no getters) by leveraging EAPLI's `RepresentationBuilder` for safe DTO projection. Business rules are enforced both at the UI level for excellent User Experience (Fast-Fail) and at the deeply encapsulated Domain level to guarantee system integrity. The architecture elegantly avoids ORM Proxy issues by fetching live entities in the Service layer, ensuring clean persistence and testability.