# US078 — Air Transport Company Collaborator Remote Access

## 1. User Story

As an Air Transport Company Collaborator, I want to remotely access the system using the Air Transport Company App.

---

## 2. Acceptance Criteria

| Criterion | Status |
|-----------|--------|
| Dedicated TCP client | Implemented (`AirTransportCompanyClientApp`) |
| TCP-only interaction (no direct database access on the client) | Implemented |
| ATCC operations available remotely | Implemented (US070–US074) |
| Authentication and authorization | Implemented (role `AIR_TRANSPORT_COMPANY_COLLABORATOR` or `ADMIN`) |
| US090 integration (remote logging) | Implemented (service identifier `US78`) |

**Note:** Pilot user stories (US075, US076, US077) are not included in this implementation because they do not yet exist in the project.

---

## 3. Remotely Exposed User Stories

| US | Operation | TCP Command |
|----|-----------|-------------|
| US070 | Add aircraft to fleet | `ADD_AIRCRAFT` |
| US071 | Decommission aircraft | `RETIRE_AIRCRAFT` |
| US072 | List company fleet | `LIST_FLEET` |
| US072a–d | List fleet by sort option | `LIST_FLEET <sort>` |
| US073 | Create flight route | `CREATE_ROUTE` |
| US074 | Deactivate flight route | `DEACTIVATE_ROUTE` |

Sort options for `LIST_FLEET`: `REGISTRATION`, `MAKER`, `MODEL`, `CAPACITY`, `AGE`.

---

## 4. Architecture

US078 shares the **unified TCP server** with US044, but is **separated** at the service, handler, and client level.

```text
AirTransportCompanyClientApp (US78)
        │
        │  TCP (default port 8044)
        ▼
RemoteAccessTcpServer
        │
        ├── SELECT ATCC
        ▼
AirTransportCompanyRemoteHandler
        │
        ├── AddAircraftController
        ├── RetireAircraftController
        ├── ListCompanyFleetController
        ├── CreateFlightRouteController
        └── DeactivateFlightRouteController
        │
        ▼
   Persistence (server-side)
```

US044 (`WeatherClientApp` + `WeatherRemoteHandler`) remains independent and uses `SELECT WEATHER`.

Main components:

| Component | Package / file |
|-----------|----------------|
| Client | `pt.isep.sem4pi.client.airtransportcompany.AirTransportCompanyClientApp` |
| Server handler | `pt.isep.sem4pi.presentation.network.remoteaccess.AirTransportCompanyRemoteHandler` |
| Service registration | `RemoteAccessService.ATCC` (`US78`) |
| TCP server | `RemoteAccessTcpServer` / `MainServer` |

---

## 5. TCP Protocol

### 5.1 Session

1. Client connects to the server.
2. `LOGIN <username> <password>`
3. Server responds with roles and service menu.
4. `SELECT ATCC`
5. ATCC business commands.
6. `BACK` — return to the service menu.
7. `LOGOUT` / `QUIT`

Responses use `OK` or `ERR` prefixes (defined in `RemoteAccessProtocol`).

### 5.2 ATCC Commands

```text
ADD_AIRCRAFT <companyIata> <registration> <manufacturer> <model> <country> <eco> <bus> <first>
RETIRE_AIRCRAFT <registration>
LIST_FLEET [REGISTRATION|MAKER|MODEL|CAPACITY|AGE]
CREATE_ROUTE <routeId> <companyIata> <originIcao> <destinationIcao>
DEACTIVATE_ROUTE <routeId> <yyyy-MM-dd>
HELP
BACK
```

### 5.3 Example — list fleet

**Client sends:**
```text
LIST_FLEET REGISTRATION
```

**Server responds:**
```text
OK 2
CS-TST;A320;Airbus;ACTIVE;180;5
CS-RET;B737;Boeing;ACTIVE;160;8
```

Each fleet line uses `;` as separator: registration, model, manufacturer, status, capacity, age.

---

## 6. Authentication and Authorization

- Login validated by `AuthenticationController` on the server.
- Each ATCC operation requires role `AIR_TRANSPORT_COMPANY_COLLABORATOR` or `ADMIN`.
- The client does **not** access repositories or the database; all logic runs on the server after TCP authentication.
- Test user (bootstrap): `atccollaborator` / `Password1`

---

## 7. Running

### Server (shared US44 + US78)

```bash
./run-weather-server.sh
# or: ./run-weather-server.sh 8044
```

### US78 client

```bash
./run-atcc-client.sh
# or: ./run-atcc-client.sh 127.0.0.1 8044
```

Optional environment variables for the server port:

- `REMOTE_ACCESS_SERVER_PORT`
- `WEATHER_SERVER_PORT` (compatibility)

---

## 8. Tests

TCP integration tests:

- `src/test/java/pt/isep/sem4pi/presentation/network/remoteaccess/AirTransportCompanyRemoteAccessIT.java`

They cover authentication, service selection, role-based authorization, and fleet listing.

Run:

```bash
mvn test
```

---

## 9. Dependencies

This user story depends on:

- US030 — authentication and authorization
- US070–US074 — already implemented business controllers
- US044 — unified TCP infrastructure (`development`)
- US090 — remote logging (identifier `US78`)

---

## 10. Known Limitations

- US075, US076, and US077 (pilot management) are **not implemented** — out of scope until domain controllers exist.
- The US78 client and the US44 client connect to the **same TCP server**; separation is done by service (`SELECT ATCC` vs `SELECT WEATHER`), not by different ports.
