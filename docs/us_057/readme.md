# US057 - Certificar um motor para um modelo de aeronave

## 1. Requirements Engineering

### 1.1. User Story Description
Como Backoffice Operator, quero adicionar um modelo de motor a lista de motores certificados de um modelo de aeronave.

### 1.2. Acceptance Criteria
* O Backoffice Operator consegue associar um motor a lista de motores certificados de um modelo de aeronave.
* O sistema impede motores duplicados para o mesmo modelo de aeronave.
* A compatibilidade do motor com o tipo do modelo de aeronave e validada antes de adicionar.

### 1.3. Dependencies
* **Autenticacao:** utilizador autenticado como Backoffice Operator ou Admin.
* **Engine Model:** deve existir pelo menos um modelo de motor registado.
* **Aircraft Model:** deve existir pelo menos um modelo de aeronave registado.

## 2. Analysis & Domain Model

A regra de negocio central vive no agregado `AircraftModel`:

* **Aggregate Roots:** `AircraftModel`, `EngineModel`.
* **Entidades:** `CertifiedEngine` (entidade interna do agregado `AircraftModel`).
* **Value Objects:** `AircraftModelId`, `EngineModelId`.

A certificacao cria um `CertifiedEngine` associado ao `AircraftModel` e valida:
1) compatibilidade com a motorization do modelo de aeronave;
2) ausencia de duplicados.

## 3. Design

### 3.1. Architecture & Flow
1. **Presentation Layer (`CertifyEngineModelUI`):** lista modelos de aeronave e motores disponiveis e permite selecionar.
2. **Controller (`CertifyEngineModelController`):** valida autorizacao e delega no servico.
3. **Application Service (`AircraftModelCertificationService`):** carrega agregados e executa a certificacao.
4. **Domain (`AircraftModel`):** aplica as regras de compatibilidade e duplicados.

### 3.2. Regras principais
* Motor incompativel gera erro antes de adicionar.
* Motor duplicado para o mesmo modelo e rejeitado.

## 4. Implementation Details

* `AircraftModel.certifyEngine(...)` centraliza as validacoes.
* `CertifiedEngine.hasEngineModel(...)` suporta a verificacao de duplicados.
* A compatibilidade compara `motorization` do `AircraftModel` com `EngineType` do `EngineModel` (case-insensitive).
* A UI apresenta listas com `modelName`, `aircraftType` e `motorization` para ajudar na selecao.

## 5. Testing

* **Certificacao com sucesso:** motor compativel e adicionado.
* **Duplicado:** tentativa de adicionar o mesmo motor gera `IllegalArgumentException`.
* **Incompativel:** motor com `EngineType` diferente da `motorization` gera `IllegalArgumentException`.

## 6. UML Diagrams (PlantUML)

Nao disponivel nesta US.

