# US071 - Retirar uma aeronave da frota

## 1. Requirements Engineering

### 1.1. User Story Description
Como colaborador de uma companhia aerea, quero retirar uma aeronave da frota da minha empresa.

### 1.2. Acceptance Criteria
* Alterar o estado de uma aeronave para retirada/desativada com sucesso.
* O sistema impede a criacao de novos voos para uma aeronave retirada.
* O sistema bloqueia a retirada se existirem voos pendentes associados a aeronave.

### 1.3. Dependencies
* **Autenticacao:** utilizador autenticado como colaborador de companhia aerea.
* **US070:** a aeronave deve existir na frota (identificada por matricula).

## 2. Analysis & Domain Model

A regra de negocio exige validacao cruzada entre Aeronave e Voo:

* **Aggregate Roots:** `Aircraft`, `Flight`.
* **Value Objects:** `AircraftRegistration`, `FlightId`.
* **Entidades:** `Aircraft` (com `AircraftStatus`), `Flight` (com `FlightStatus`).

A retirada e permitida apenas quando nao existem voos pendentes para a aeronave.

## 3. Design

### 3.1. Architecture & Flow
1. **Presentation Layer (`RetireAircraftUI`):** lista aeronaves ativas, permite selecao e mostra mensagens de erro/sucesso.
2. **Controller (`RetireAircraftController`):** valida permissao e delega ao servico de aplicacao.
3. **Application Service (`AircraftRetirementService`):** verifica voos pendentes e altera o estado para `RETIRED`.
4. **Repositories:**
   * `AircraftRepository` para carregar e guardar a aeronave.
   * `FlightRepository` para validar voos pendentes (`hasPendingFlights`).

### 3.2. Regras principais
* Se existir voo pendente, a retirada falha com erro.
* Aeronave retirada nao pode ser usada em novas criacoes de voo.

## 4. Implementation Details

* O estado e controlado por `AircraftStatus` (`ACTIVE`, `RETIRED`).
* A validacao de voos pendentes e centralizada em `FlightRepository.hasPendingFlights`.
* A criacao de voos usa `FlightSchedulingService`, que bloqueia aeronaves retiradas.

## 5. Testing

* **Retirada com sucesso:** aeronave sem voos pendentes passa a `RETIRED`.
* **Bloqueio com voos pendentes:** tentativa de retirada gera `IllegalStateException`.

## 6. UML Diagrams (PlantUML)

Nao disponivel nesta US.

