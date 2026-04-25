# US-031: Register Backoffice Users

## 1. Contexto
Permitir que um utilizador com a role `ADMIN` registe novos utilizadores internos de backoffice, onde cada utilizador tem credenciais (para autenticação via EAPLI) e dados adicionais de domínio (nome, email único, telefone, security clearance expiration date e skills assessment date).

## 2. Arquitectura / Organização
- Domínio: `pt.isep.sem4pi.domain.usermanagement` — nova entidade `BackofficeUser` e interfaces de suporte.
- Aplicação: `pt.isep.sem4pi.application.usermanagement` — `RegisterBackofficeUserController`.
- Persistência: `pt.isep.sem4pi.infrastructure.persistence` — `InMemoryBackofficeUserRepository`, alterações em `RepositoryFactory` e seed em `InMemoryRepositoryFactory`.
- Apresentação: `pt.isep.sem4pi.presentation.usermanagement` — `RegisterBackofficeUserUI`; menu em `pt.isep.sem4pi.presentation.MainMenu`.
- Integração com EAPLI para autenticação: `SystemUser`, `UserBuilderHelper`, `AuthzRegistry`.

## 3. Ficheiros criados e responsabilidade
- `src/main/java/pt/isep/sem4pi/domain/usermanagement/BackofficeUser.java` — entidade dos dados adicionais (username, name, email, phoneNumber, securityClearanceExpirationDate, skillsAssessmentDate).
- `src/main/java/pt/isep/sem4pi/domain/usermanagement/BackofficeUserRepository.java` — interface do repositório.
- `src/main/java/pt/isep/sem4pi/infrastructure/persistence/InMemoryBackofficeUserRepository.java` — implementação em memória (maps por username e email).
- `src/main/java/pt/isep/sem4pi/infrastructure/persistence/RepositoryFactory.java` — adicionado `backofficeUsers()`.
- `src/main/java/pt/isep/sem4pi/infrastructure/persistence/InMemoryRepositoryFactory.java` — implementação e seed/bootstrapping.
- `src/main/java/pt/isep/sem4pi/application/usermanagement/RegisterBackofficeUserController.java` — validações, criação de `SystemUser` (via `UserBuilderHelper`) e de `BackofficeUser`.
- `src/main/java/pt/isep/sem4pi/presentation/usermanagement/RegisterBackofficeUserUI.java` — prompts de consola; validação por campo e re-prompt.
- `src/main/java/pt/isep/sem4pi/presentation/MainMenu.java` — adição da opção no bloco Admin.

## 4. Fluxo da funcionalidade
1. Administrador faz login (US030).
2. No `MainMenu` → Admin seleciona `Register Backoffice User`.
3. A UI pede, um a um, os campos (username, password, first/last name, email, phone, security clearance date, skills assessment date), re-pedindo em caso de validação falhada.
4. O controller valida (autorização, política de password, unicidade do email/username) e cria:
   - `SystemUser` (guardado em `repositories().users()`) com role `BACKOFFICE_OPERATOR`;
   - `BackofficeUser` (guardado em `repositories().backofficeUsers()`).
5. O novo utilizador pode autenticar imediatamente com as credenciais criadas.

## 5. Registo de Backoffice Users
- Autorização: só `ADMIN` pode executar a operação (verificação via `AuthzRegistry.authorizationService()`).
- Validações principais:
  - Password validada por `SEM4PIPasswordPolicy`.
  - Email validado pelo formato (`EmailAddress.valueOf`) e verificado como único no repositório de backoffice.
  - Username e email verificados para não duplicação no `BackofficeUserRepository`.
  - Phone e ambas as datas são campos obrigatórios (validados no domínio e na UI).
- Roles: o `SystemUser` recebe `SEM4PIRoles.BACKOFFICE_OPERATOR`.

## 6. Persistência (InMemory)
- `InMemoryBackofficeUserRepository` armazena `BackofficeUser` em memória (mapas por username e email em lowercase).
- `InMemoryRepositoryFactory` expõe `backofficeUsers()` e faz seed inicial:
  - cria `admin` e um `SystemUser` de backoffice (`bo`) via `seedDefaultUsers`;
  - cria um `BackofficeUser` associado a `bo` via `seedDefaultBackofficeUsers`, evitando duplicação.
- A persistência do `SystemUser` usa o repositório do EAPLI (`repositories().users()`).

## 7. Integração com US030
- A autenticação mantém-se inalterada: `SEM4PIBackoffice` configura `AuthzRegistry` com `PersistenceContext.repositories().users()` e `SEM4PIPasswordPolicy`.
- O `RegisterBackofficeUserController` cria o `SystemUser` (auth) e o `BackofficeUser` (domínio). O `LoginUI` existente autentica os `SystemUser` criados.

## 8. Utilizadores de teste / seed actual
- Seed em `InMemoryRepositoryFactory`:
  - `admin` / `Password1` — role `ADMIN`.
  - `bo` / `Password1` — role `BACKOFFICE_OPERATOR` (SystemUser) + `BackofficeUser` com telefone e datas preenchidas.
- O seed verifica existência antes de criar, evitando duplicações.

## 9. Testes manuais possíveis
- Run (IDE ou Maven): compilar e executar `pt.isep.sem4pi.Main`.
- Login administrador: `admin` / `Password1`.
- Registar novo backoffice user: `MainMenu` → Admin → Register Backoffice User; seguir prompts (UI re-pede campos inválidos).
- Testar login com o novo utilizador: autenticar com o username/password criados; verificar role `BACKOFFICE_OPERATOR`.

## 10. Conclusão
US031 implementa o registo controlado de utilizadores de backoffice respeitando a autenticação EAPLI e persistência em memória. A solução separa credenciais (SystemUser) dos dados de domínio (BackofficeUser), inclui validações essenciais e uma seed para testes.
