# Alterações ao Domain Model DDD

## 1. Objetivo

Este documento resume as principais alterações feitas ao Domain Model inicial, a justificação dessas alterações e o papel de cada agregado no domínio.

O objetivo foi alinhar o modelo com:

* o enunciado;
* as respostas do cliente;
* o email com orientações sobre a modelação;

---

## 2. Alterações principais ao modelo

### 2.1. Divisão em vários DDDs

O modelo deixou de estar concentrado num único diagrama grande e passou a estar dividido em vários diagramas:

* DDD principal com agregados e relações principais;
* Identity & Access / Customer Management;
* Backoffice Configuration;
* Air Transport Operations & Flight Planning;
* Weather & Simulation Integration.

**Porquê:**
O professor sugeriu separar o modelo num artefacto principal e outros mais detalhados. Isto melhora a legibilidade e facilita a defesa oral.

---

### 2.2. Separação entre `FlightRoute`, `Flight` e `FlightPlan`

No modelo antigo, a distinção entre route, flight e plan não estava clara, e `FlightType` estava em `FlightRoute`.

No modelo novo:

* `FlightRoute` representa a rota da companhia;
* `Flight` representa a instância operacional/calendarizada da route;
* `FlightPlan` representa o documento associado à flight.

**Porquê:**
O cliente clarificou a relação:
`Route - 1:N - Flight - 1:N - FlightPlan`

**Alteração importante:**
`FlightType` saiu de `FlightRoute` e passou para `Flight`, porque regular e charter dizem respeito à execução calendarizada do voo, não à identidade da rota.

---

### 2.3. Introdução de scheduling em `Flight`

Foi criada `FlightScheduleEntry` para representar o horário recorrente.

**Porquê:**
O cliente clarificou que:

* charter flights têm uma única instância;
* regular flights têm horários recorrentes;
* a forma mais flexível é guardar dia da semana e hora para cada entrada.

---

### 2.4. `CompanyCollaborator` foi substituído por `CustomerCollaborator`

No modelo antigo existia `CompanyCollaborator`.

No novo modelo passou a existir `CustomerCollaborator`.

**Porquê:**
O enunciado fala em **customer collaborators**, e o customer pode ser:

* uma `AirTransportCompany`;
* uma `AirControlArea`.

Por isso, `CompanyCollaborator` era demasiado restritivo.

---

### 2.5. `AirControlArea` passou a ter boundary retangular explícito

No modelo antigo existia `GeographicBoundary` com uma descrição textual.

No novo modelo passou a existir `AirControlAreaBoundary` com:

* `minLatitude`
* `maxLatitude`
* `minLongitude`
* `maxLongitude`

**Porquê:**
O cliente esclareceu que, no projeto, a área pode ser simplificada como um retângulo.

Isto também ajuda a justificar melhor:

* validade do boundary;
* pertença dos aeroportos à área;
* regra de não sobreposição entre áreas.

---

### 2.6. `Airport` manteve-se como agregado próprio

`Airport` continuou a ser agregado próprio, mas ficou mais explícito que:

* pertence a exatamente uma `AirControlArea`;
* uma `AirControlArea` pode ter vários aeroportos.

**Porquê:**
O cliente confirmou estas regras.
Assim, `Airport` faz mais sentido como agregado próprio do que como entidade interna da área.

---

### 2.7. `Manufacturer` manteve-se único

Foi mantido um único `Manufacturer` para:

* `EngineModel`
* `AircraftModel`

**Porquê:**
O cliente confirmou que a mesma entidade pode fabricar motores e aeronaves.

---

### 2.8. Reforço da relação entre `AircraftModel`, engines certificados e `Aircraft`

O modelo novo distingue melhor:

* engines certificados para um modelo;
* engine efetivamente usado num aircraft.

Para isso:

* `AircraftModel` passou a ter `CertifiedEngine`;
* `Aircraft` passou a guardar `aircraftModelId` e `engineModelId`.

**Porquê:**
Era importante separar o que é compatível com o modelo do que está realmente instalado no aircraft.

---

### 2.9. `Pilot` passou a ter certificações explícitas

Foi criada `PilotCertification` ligada a `Pilot`.

**Porquê:**
O enunciado diz que um piloto é certificado para um ou mais aircraft models.
Sem isso, o modelo ficava incompleto.

---

### 2.10. Remoção de detalhe excessivo do centro do DDD

Elementos como:

* `RouteDefinition`
* `AltitudeSlot`
* `FlightLeg`
* `Segment`
* `Node`

deixaram de estar no centro do DDD principal.

**Porquê:**
O cliente indicou que duvida que `FlightLegs` sejam necessários no DDD Domain Model nesta fase.
Além disso, o foco do Sprint 1 é o core domain em Java, não o detalhe completo do DSL.

---

### 2.11. Simplificação da simulação no core Java

No modelo antigo a simulação estava demasiado detalhada, com:

* `SimulatedFlight`
* `Position`
* `SafetyViolation`

No novo modelo, a simulação passou a focar-se em:

* pedido de simulação;
* inputs;
* report;
* resumos dos resultados recebidos do SCOMP.

**Porquê:**
O cliente clarificou que a simulação detalhada pertence ao módulo em C.
No core Java, o sistema apenas envia informação ao simulador e recebe resultados.

---

### 2.12. `SimulationReport` deixou de ser `ValueObject`

No modelo antigo, `SimulationReport` era `ValueObject`.

No novo modelo, passou a ser um agregado próprio.

**Porquê:**
O report tem identidade, associação a uma simulação, data de geração e localização de ficheiro.
Por isso, comporta-se mais como entidade persistida do que como simples VO.

---

### 2.13. Especialização do `SimulationReport` para a US111

O agregado `SimulationReport` passou a incluir explicitamente:

* `FlightExecutionSummary`, com a identificação do voo e o seu estado final;
* `SafetyViolationSummary`, com timestamp, tipo, posição e detalhes;
* `ReportFilePath`, que encapsula a localização do ficheiro exportado;
* `SimulationValidationResult`, com os valores `PASS` e `FAIL`.

O conceito genérico `SimulationIssue` foi removido porque não representava com
precisão os dados exigidos pela US111. Os novos conceitos continuam a ser
resumos no core Java; o detalhe da simulação e a deteção das violações permanecem
no componente SCOMP.

**Porquê:**
O relatório precisa de guardar e apresentar o número e estado dos voos, bem como
timestamps e posições das violações. Estes dados pertencem ao ciclo de vida do
`SimulationReport`, não têm identidade própria e são, por isso, Value Objects do
agregado.

---

## 3. Papel de cada agregado

### `User`

Representa um utilizador do sistema.
Guarda nome, email, telefone, estado, security clearance e skills assessment.

### `Role`

Representa um papel de autorização atribuído a um utilizador.

### `AirTransportCompany`

Representa uma companhia aérea registada no sistema.

### `CustomerCollaborator`

Representa um utilizador associado a um customer do sistema, como companhia aérea ou área de controlo.

### `Pilot`

Representa um piloto enquanto conceito de negócio, com estado próprio e certificações.

### `Manufacturer`

Representa um fabricante de motores e/ou aeronaves.

### `EngineModel`

Representa um modelo de motor.

### `AircraftModel`

Representa um modelo de aeronave, incluindo características físicas e operacionais.

### `AirControlArea`

Representa uma área de controlo aéreo com código, nome, regras e boundary.

### `Airport`

Representa um aeroporto associado a uma única air control area.

### `Aircraft`

Representa uma aeronave concreta da frota de uma companhia.

### `FlightRoute`

Representa a rota comercial da companhia entre dois aeroportos.

### `Flight`

Representa a instância operacional/calendarizada de uma route.

### `FlightPlan`

Representa o plano/documento associado a uma flight.

### `WeatherSource`

Representa uma fonte externa de dados meteorológicos.

### `WeatherImportBatch`

Representa uma importação em lote de weather data.

### `WeatherData`

Representa um registo meteorológico consultável no sistema.

### `Simulation`

Representa um pedido de simulação no core system.

### `SimulationReport`

Representa o resumo final persistido de uma simulação. Mantém os estados finais
dos voos, as violações de segurança, a localização do ficheiro e o resultado
global de validação.

---

## 4. Principais diferenças face ao DDD antigo

Comparando com o modelo antigo, as mudanças mais relevantes foram:

* divisão do modelo em vários diagramas;
* `FlightType` saiu de `FlightRoute` e passou para `Flight`;
* introdução clara de `Flight` entre `FlightRoute` e `FlightPlan`;
* `CompanyCollaborator` foi substituído por `CustomerCollaborator`;
* `GeographicBoundary` textual foi substituído por boundary retangular explícito;
* `Pilot` passou a ter certificações;
* `AircraftModel` passou a controlar explicitamente engines certificados;
* o detalhe técnico do DSL deixou de estar no centro do DDD;
* a parte da simulação foi simplificada no core Java;
* `SimulationReport` deixou de ser VO;
* a US111 especializou o report com resumos de execução, violações, caminho do
  ficheiro e resultado de validação.

---
