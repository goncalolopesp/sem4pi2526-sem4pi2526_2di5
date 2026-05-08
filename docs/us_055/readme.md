# US-055: Add Aircraft Model

## 1. Context
Implementation of the functionality that allows a `BACKOFFICE_OPERATOR` to register a new Aircraft Model in the SEM4PI system. This includes defining its physical characteristics, wing geometry, performance metrics, fuel capacity, and the compatible engine models.

## 2. Architecture / Organization
- `domain.model.aircraftModel`: `AircraftModel`, `AircraftModelId`, `AircraftType`, `MotorizationType`, `Weight`, `WingGeometry`, `Fuel`, `AircraftPerformance`, `Coefficient`.
- `application`: `AddAircraftModelController`.
- `infrastructure.bootstrapers`: `AircraftModelBootstrapper` (structured and awaiting US056 data).
- `infrastructure.persistence`: `InMemoryAircraftModelRepository`.
- `presentation.aircraftModel`: `AddAircraftModelUI`, `AddAircraftModelAction`.
- `presentation`: `MainMenu`.

## 3. Created files and core responsibilities
- `src/main/java/pt/isep/sem4pi/domain/model/aircraftModel/AircraftModel.java`
    - The aggregate root for the Aircraft Model. Groups all value objects and ensures the entity's internal consistency.

- `src/main/java/pt/isep/sem4pi/domain/model/aircraftModel/* (Value Objects)`
    - Classes like `Weight`, `AircraftPerformance`, and `WingGeometry` validate domain rules (e.g., rejecting negative values or null inputs) using `BigDecimal` to ensure precision in physical calculations. `AircraftType` and `MotorizationType` are implemented as Enums.

- `src/main/java/pt/isep/sem4pi/application/AddAircraftModelController.java`
    - Orchestrates the use case. Receives raw data from the UI, enforces authorization, checks for domain duplication rules via the repository, constructs the Value Objects, and saves the new entity. Temporarily uses dummy data for dependencies until US056 is completed.

- `src/main/java/pt/isep/sem4pi/infrastructure/persistence/InMemoryAircraftModelRepository.java`
    - In-memory persistence implementation. Implements the `containsOfManufacturerAndName` method using Java Streams to iterate over saved instances instead of JPQL.

- `src/main/java/pt/isep/sem4pi/presentation/aircraftModel/AddAircraftModelUI.java` / `AddAircraftModelAction.java`
    - UI layer that interacts with the user. Utilizes EAPLI's `SelectWidget` to provide error-proof selections for Manufacturers, Aircraft Types, and Engine Models. Catches `IllegalArgumentException` from the domain to prevent application crashes on invalid inputs.

- `src/test/java/pt/isep/sem4pi/application/AddAircraftModelControllerTests.java`
    - JUnit 5 test class. Uses real `InMemory` repositories to accurately test business rules (like duplication prevention) and uses Mockito solely to mock the `AuthorizationService`.

## 4. Application Flow
`MainMenu` (Operator Menu) → `AddAircraftModelAction.execute()` → `AddAircraftModelUI.show()` → Prompts user for selections via `SelectWidget` and manual `BigDecimal` inputs → Calls `AddAircraftModelController.addAircraftModel(...)` → Controller validates authorization → Controller checks `aircraftRepo` for duplicate names under the same manufacturer → Instantiates Value Objects and `AircraftModel` → `aircraftRepo.save()` → Returns success message to UI.

## 5. Domain Rules & Validation
- **Data Integrity:** All physical attributes are encapsulated in Value Objects that throw `IllegalArgumentException` if instantiated with invalid values (e.g., negative weights).
- **Business Rule:** The system prevents the creation of an Aircraft Model if the specified Manufacturer already has an Aircraft Model with the exact same name.

## 6. Authorization
- Protected by `SEM4PIRoles`.
- `AddAircraftModelController` uses `authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR)` to guarantee that only authorized staff can execute the use case.

## 7. Persistence (InMemory)
- Currently relies on `InMemoryAircraftModelRepository` extending EAPLI's `InMemoryDomainRepository`.
- Due to the lack of a real database at this stage, queries are performed programmatically via `Iterable` and `StreamSupport`.
- `AircraftModelBootstrapper` is drafted but currently commented out, pending the completion of Manufacturer and Engine Model seeds from US056 to maintain referential integrity.

## 8. Testing Strategy
- The controller is tested using a hybrid approach. The database is not mocked; instead, real instances of `InMemoryAircraftModelRepository` are instantiated in the `@BeforeEach` setup.
- This forces the execution of the actual repository logic, ensuring that the NFR08 (In-Memory capability) is fulfilled and that domain duplication validations work flawlessly in memory.

## 9. Manual testing steps (Quick)
- Log in as `bo` / `Password1`.
- Navigate to `Operator >` -> `Add New Aircraft Model`.
- Select a Manufacturer and an Aircraft Type using the numbered menus.
- Input valid numbers for the requested specifications.
- Select at least one Engine Model.
- Verify the success message.
- Repeat the process selecting the **same** Manufacturer and typing the **same** Model Name to trigger and observe the domain duplication error gracefully handled by the UI.

## 10. Conclusion
US055 is fully implemented as a complete vertical slice. It leverages the EAPLI framework's UI widgets for a robust user experience, strictly follows Domain-Driven Design principles with comprehensive Value Objects, and ensures business rules are validated and tested securely within the application and domain layers.