# US086 — Pilot Remote Access

## 1. User Story

As a Pilot, I want to remotely access the system using a dedicated TCP client application.

---

## 2. Acceptance Criteria

| Critério | Estado |
|----------|--------|
| Cliente TCP dedicado | Implementado (`PilotClientApp`) |
| Interação limitada a TCP (sem BD no cliente) | Implementado |
| User stories de Pilot disponíveis remotamente | Implementado (US080–US082, US085) |
| Autenticação e autorização | Implementado (role `PILOT` ou `ADMIN`) |
| Integração com US090 (logging remoto) | Implementado (identificador `US86`) |

**Nota:** US075–US077 são user stories de **Air Transport Company Collaborator** (US78), não de Pilot — não fazem parte do scope da US86.

---

## 3. User Stories expostas remotamente

| US | Operação | Comando TCP |
|----|----------|-------------|
| US080 | Create flight plan | `CREATE_FLIGHT_PLAN` |
| US081 | Import flight plan from DSL file | `IMPORT_PLAN_BEGIN` + linhas DSL |
| US082 | Associate weather data with flight plan | `LIST_WEATHER`, `ASSOCIATE_WEATHER` |
| US085 | Test/validate flight plan | `LIST_DRAFT_PLANS`, `TEST_FLIGHT_PLAN` |

Comandos auxiliares: `LIST_ROUTES`, `LIST_FLIGHTS`, `LIST_MY_PLANS`.

---

## 4. Arquitectura

```text
PilotClientApp (US86)
        │
        │  TCP (porta 8044)
        ▼
RemoteAccessTcpServer
        │
        ├── SELECT PILOT
        ▼
PilotRemoteHandler
        │
        ├── CreateFlightPlanController (US080)
        ├── ImportFlightPlanController (US081)
        ├── AssociateWeatherDataController (US082)
        └── TestFlightPlanController (US085)
        │
        ▼
   Persistência (servidor)
```

---

## 5. Protocolo TCP

### Sessão

1. Cliente liga ao servidor.
2. `LOGIN <username> <password>`
3. `SELECT PILOT`
4. Comandos de negócio.
5. `BACK` — menu de serviços.
6. `LOGOUT` / `QUIT`

### Exemplos

```text
CREATE_FLIGHT_PLAN TP1001 TP100 REGULAR PILOT01 20 6 2026 14 30 0 15 20 0 4200 KG
ASSOCIATE_WEATHER FP-DEMO-001 1,2
TEST_FLIGHT_PLAN FP-DEMO-001
IMPORT_PLAN_BEGIN 11
<linhas do ficheiro .dsldig5>
```

---

## 6. Execução

### Servidor (partilhado)

```bash
./run-main-server.sh
# ou: ./run-weather-server.sh
```

### Cliente US86

```bash
./run-pilot-client.sh
# ou: ./run-pilot-client.sh 127.0.0.1 8044
```

Utilizador de teste: `pilot` / `Password1`

---

## 7. Testes

- `src/test/java/pt/isep/sem4pi/presentation/network/remoteaccess/PilotRemoteAccessIT.java`

```bash
mvn test
```

---

## 8. Dependências

- US030 — autenticação
- US080–US085 — controllers de flight plan
- US044 — infraestrutura TCP unificada
- US090 — logging remoto (`US86`)
