# US086 — Pilot Remote Access

## 1. User Story

As a Pilot, I want to remotely access the system using a dedicated TCP client application.

---

## 2. Acceptance Criteria

| Criterion | Status |
|-----------|--------|
| Dedicated TCP client | Implemented (`PilotClientApp`) |
| TCP-only interaction (no database access on the client) | Implemented |
| Pilot user stories available remotely | Implemented (US080–US082, US085) |
| Authentication and authorization | Implemented (role `PILOT` or `ADMIN`) |
| US090 integration (remote logging) | Implemented (identifier `US86`) |

**Note:** US075–US077 are **Air Transport Company Collaborator** user stories (US78), not Pilot stories — they are out of scope for US86.

---

## 3. Remotely Exposed User Stories

| US | Operation | TCP Command |
|----|-----------|-------------|
| US080 | Create flight plan | `CREATE_FLIGHT_PLAN` |
| US081 | Import flight plan from DSL file | `IMPORT_PLAN_BEGIN` + DSL lines |
| US082 | Associate weather data with flight plan | `LIST_WEATHER`, `ASSOCIATE_WEATHER` |
| US085 | Test/validate flight plan | `LIST_DRAFT_PLANS`, `TEST_FLIGHT_PLAN` |

Auxiliary commands: `LIST_ROUTES`, `LIST_FLIGHTS`, `LIST_MY_PLANS`.

---

## 4. Architecture

```text
PilotClientApp (US86)
        │
        │  TCP (port 8044)
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
   Persistence (server-side)
```

---

## 5. TCP Protocol

### Session

1. Client connects to the server.
2. `LOGIN <username> <password>`
3. `SELECT PILOT`
4. Business commands.
5. `BACK` — return to the service menu.
6. `LOGOUT` / `QUIT`

### Examples

```text
CREATE_FLIGHT_PLAN TP1001 TP100 REGULAR PILOT01 20 6 2026 14 30 0 15 20 0 4200 KG
ASSOCIATE_WEATHER FP-DEMO-001 1,2
TEST_FLIGHT_PLAN FP-DEMO-001
IMPORT_PLAN_BEGIN 11
<.dsldig5 file lines>
```

---

## 6. Running

### Server (shared)

```bash
./run-main-server.sh
# or: ./run-weather-server.sh
```

### US86 client

```bash
./run-pilot-client.sh
# or: ./run-pilot-client.sh 127.0.0.1 8044
```

Test user: `pilot` / `Password1`

---

## 7. Tests

- `src/test/java/pt/isep/sem4pi/presentation/network/remoteaccess/PilotRemoteAccessIT.java`

```bash
mvn test
```

---

## 8. Dependencies

- US030 — authentication
- US080–US085 — flight plan controllers
- US044 — unified TCP infrastructure
- US090 — remote logging (`US86`)
