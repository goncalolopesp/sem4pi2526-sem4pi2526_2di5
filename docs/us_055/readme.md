# US-033: List Users

## 1. Context
Implementation of the functionality that allows an `ADMIN` to list all the backoffice users in the SEM4PI system. The listing must include essential user details such as username, full name, assigned roles, and their current system status (Active/Inactive). It strictly filters out non-backoffice users (e.g., clients).

## 2. Architecture / Organization
- `domain.usermanagement`: `SEM4PIRoles`.
- `application`: `ListUsersController`.
- `infrastructure.persistence`: `UserRepository` (provided by EAPLI), `InMemoryUserRepository`.
- `presentation.users`: `ListUsersUI`, `ListUsersAction`.
- `presentation`: `MainMenu`.

## 3. Created files and core responsibilities
- `src/main/java/pt/isep/sem4pi/application/ListUsersController.java`
    - Orchestrates the use case. Fetches all users from the database, enforces authorization rules, and actively filters the dataset to return only users who possess backoffice-related roles (`ADMIN` or `BACKOFFICE_OPERATOR`).

- `src/main/java/pt/isep/sem4pi/presentation/users/ListUsersUI.java` / `ListUsersAction.java`
    - UI layer responsible for formatting and displaying the data in the console. It iterates over the filtered list provided by the controller, extracts the `SystemUser` roles using Streams, evaluates the `isActive()` boolean to output a clear "ACTIVE" or "INACTIVE" status string, and prints an aligned table-like view.

- `src/test/java/pt/isep/sem4pi/application/ListUsersControllerTest.java`
    - JUnit 5 test class. Uses `Mockito` to mock the EAPLI `AuthorizationService` and `UserRepository`. Verifies that the filtering logic correctly isolates backoffice users and ignores system users with strictly external roles (like clients).

## 4. Application Flow
`MainMenu` (Admin Menu) → `ListUsersAction.execute()` → `ListUsersUI.show()` → Calls `ListUsersController.allBackofficeUsers()` → Controller validates `ADMIN` authorization → Controller requests `userRepo.findAll()` → Controller filters the list checking `user.hasAny(ADMIN, BACKOFFICE_OPERATOR)` → Returns filtered list to UI → UI maps `roleTypes()` and evaluates `isActive()` → Prints formatted data to the console.

## 5. Domain Rules & Validation
- **Backoffice Scope Rule:** The system must not display clients, passengers, or external stakeholders in this specific menu. This business rule is enforced in the Controller layer via role verification.
- **Status Representation:** The "Status" requested by the client is inherently tied to the EAPLI `SystemUser` aggregate root's active state, abstracted via the `isActive()` method rather than creating redundant domain objects.

## 6. Authorization
- Protected by `SEM4PIRoles`.
- `ListUsersController` strictly uses `authz.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.ADMIN)` to guarantee that only Administrators can view the user roster.

## 7. Persistence (InMemory)
- Completely relies on the pre-existing `InMemoryUserRepository` initialized during US030.
- No new repositories or factories were needed, fully leveraging the `PersistenceContext` and EAPLI's built-in user management infrastructure.

## 8. Testing Strategy
- A mock-based approach was chosen since `SystemUser` is an external aggregate from the EAPLI framework with complex instantiation rules (Builders, Password Policies).
- `Mockito` is used to simulate different user profiles (Admin, Backoffice, Client) returned by the repository, ensuring the controller's filtering logic is isolated and verified accurately.

## 9. Manual testing steps (Quick)
- Log in as `admin` / `Password1`.
- Navigate to `Users >` -> `List all Users`.
- Verify that the console displays the registered users (e.g., `admin` and `bo`).
- Confirm that the `Roles` column shows the correct roles separated by commas.
- Confirm that the `Status` column accurately displays `ACTIVE`.
- Attempt to access the menu using a non-admin account (e.g., `bo`) to verify that the menu option is properly hidden or blocks access.

## 10. Conclusion
US033 was implemented efficiently by reusing the EAPLI framework's existing `SystemUser` domain and infrastructure. It fulfills the client's requirements with a robust filtering mechanism in the controller to segregate internal staff from external users, supported by a clean, formatted UI and isolated unit tests.