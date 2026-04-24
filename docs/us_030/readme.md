# US-030: Authentication and authorization

## 1. Contexto
Implementação da autenticação e autorização do backoffice SEM4PI com EAPLI: login/logout, política de passwords, mudança de password e controlo de opções de menu por roles (`ADMIN`, `BACKOFFICE_OPERATOR`).

## 2. Arquitectura / Organização
- `domain.usermanagement`: `SEM4PIPasswordPolicy`, `SEM4PIRoles`, `UserBuilderHelper`.  
- `infrastructure.bootstrap`: `MasterUsersBootstrapper` (bootstrap comentado).  
- `infrastructure.persistence`: `PersistenceContext`, `InMemoryRepositoryFactory`.  
- `presentation/authz`: `LoginUI`, `LoginAction`, `LogoutUI`, `LogoutAction`, `ChangePasswordUI`, `MyUserMenu`.  
- `presentation`: `MainMenu`, `SEM4PIBackoffice`, `Main`.

## 3. Ficheiros criados e responsabilidade (essencial)
- `src/main/java/pt/isep/sem4pi/domain/usermanagement/SEM4PIPasswordPolicy.java`  
  - Valida passwords (>=6, contém dígito e maiúscula) e classifica força (`WEAK`, `GOOD`, `EXCELLENT`). Usada por builders e `AuthzRegistry`.

- `src/main/java/pt/isep/sem4pi/domain/usermanagement/SEM4PIRoles.java`  
  - Define roles do projecto (`ADMIN`, `BACKOFFICE_OPERATOR`, etc.). Usada para autorizações e seed.

- `src/main/java/pt/isep/sem4pi/domain/usermanagement/UserBuilderHelper.java`  
  - Fornece `SystemUserBuilder` configurado com `SEM4PIPasswordPolicy` e `PlainTextEncoder`. Usada no seed e bootstrap.

- `src/main/java/pt/isep/sem4pi/infrastructure/bootstrap/MasterUsersBootstrapper.java`  
  - Cria utilizadores "master" (`admin`, `backoffice`) — existe mas a chamada está comentada.

- `src/main/java/pt/isep/sem4pi/infrastructure/persistence/InMemoryRepositoryFactory.java`  
  - Factory em memória; cria `InMemoryUserRepository` e faz seed de `admin` e `bo`.

- `src/main/java/pt/isep/sem4pi/infrastructure/persistence/PersistenceContext.java`  
  - Ponto de acesso à `RepositoryFactory`. Por defeito devolve a factory em memória.

- `src/main/java/pt/isep/sem4pi/presentation/authz/LoginUI.java` / `LoginAction.java`  
  - UI e acção de login (até 3 tentativas). Usa `AuthzRegistry.authenticationService().authenticate(...)`.

- `src/main/java/pt/isep/sem4pi/presentation/authz/LogoutUI.java` / `LogoutAction.java`  
  - Limpa sessão via `AuthzRegistry.authorizationService().clearSession()`.

- `src/main/java/pt/isep/sem4pi/presentation/authz/ChangePasswordUI.java`  
  - UI para alteração de password. Lê `Old Password` e `New Password` via `Console` e chama `AuthzRegistry.authenticationService().changePassword(old, new)`. Trata `ConcurrencyException` e `IntegrityViolationException`. Retorna sucesso/falha. É chamada apenas a partir de `MyUserMenu` (que só a expõe quando há sessão).

- `src/main/java/pt/isep/sem4pi/presentation/authz/MyUserMenu.java`  
  - Submenu "My account >": mostra `Login` quando anónimo ou `Change password`, `Change user`, `Logout` quando autenticado.

- `src/main/java/pt/isep/sem4pi/presentation/MainMenu.java`  
  - Menu principal; headline mostra utilizador autenticado. Mostra submenu `Users` se `BACKOFFICE_OPERATOR` ou `ADMIN`; blocos admin apenas para `ADMIN`.

- `src/main/java/pt/isep/sem4pi/presentation/SEM4PIBackoffice.java`  
  - Configura `AuthzRegistry` com o repositório de utilizadores, `SEM4PIPasswordPolicy` e `PlainTextEncoder`. Inicia `LoginUI` → `MainMenu`.

- `src/main/java/pt/isep/sem4pi/Main.java`  
  - Delegate para `SEM4PIBackoffice`.

## 4. Fluxo da aplicação
`Main` → `SEM4PIBackoffice.main()` configura `AuthzRegistry` (users repo, policy, encoder) → `PersistenceContext` devolve factory em memória (seed) → `LoginUI.show()` autentica → em sucesso `MainMenu.mainLoop()` → através de `MyUserMenu` usa-se `ChangePasswordUI` ou `LogoutUI` → logout limpa sessão e volta ao estado anónimo.

## 5. Autenticação
- `SEM4PIBackoffice` chama `AuthzRegistry.configure(PersistenceContext.repositories().users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder())`.  
- `LoginUI` pede credenciais e usa `authenticationService.authenticate(username, password, onlyWithThis)`.  
- `PlainTextEncoder` é usado (sem hashing a nível do encoder passado).  
- `ChangePasswordUI` usa `authenticationService.changePassword(old, new)` para alterar a password da sessão autenticada.

## 6. Autorização
- Roles em `SEM4PIRoles`.  
- `MainMenu` usa `authz.isAuthenticatedUserAuthorizedTo(...)` para condicionar entrada `Users` (BACKOFFICE_OPERATOR ou ADMIN) e blocos admin (`ADMIN`).  
- `LoginUI` suporta `onlyWithThis` para exigir role específica no processo de autenticação.

## 7. Persistência (InMemory)
- `PersistenceContext` retorna `InMemoryRepositoryFactory` (comentário explícito no código: default para US030).  
- Seed de utilizadores realizado em `InMemoryRepositoryFactory.seedDefaultUsers(...)`. `MasterUsersBootstrapper` existe mas não é invocado por defeito.

## 8. Utilizadores de teste (seed actual)
- `admin` / `Password1` — role: `ADMIN`.  
- `bo` / `Password1` — role: `BACKOFFICE_OPERATOR`.  
(Nota: `MasterUsersBootstrapper` define `backoffice` em vez de `bo`, mas não é executado por defeito.)

## 9. Testes manuais possíveis (rápidos)
- Login válido: `admin`/`Password1` → acesso admin; `bo`/`Password1` → vê submenu `Users`.  
- Login inválido: credenciais erradas até 3 tentativas → `LoginUI` falha.  
- Change password:
  - Autenticar; selecionar `My account > Change password`; inserir password antiga correta e nova válida → deve retornar sucesso.  
  - Tentar com password antiga errada → operação falha.  
  - Exceções de concorrência/integridade são capturadas e impressas pela UI.  
- Logout: executar `Logout` → sessão limpa; `headline` passa a `==Anonymous==`.

## 10. Conclusão
US030 está implementada de forma funcional e auto‑contida: autenticação/configuração via `AuthzRegistry` com persistência em memória, política de passwords (`SEM4PIPasswordPolicy`), UI para login/logout e alteração de password (`ChangePasswordUI`), e autorização por roles que condiciona entradas do menu.