# US044 — Weather Person Remote Access

## 1. Requirements Engineering

### 1.1 User Story

As a **Weather Person**, I want to access weather-related functionality remotely over TCP so that I can register, import, and consult weather data without using the local console UI.

### 1.2 Acceptance Criteria

- Remote clients connect to the SEM4PI TCP server and authenticate with username/password.
- After login, users with the `WEATHER_PERSON` role can select the Weather Data service (`US44`).
- Supported operations mirror US041, US042, and US043:
  - Register weather data (`REGISTER`)
  - Import bulk weather data (`IMPORT` / `IMPORT_BEGIN`)
  - Consult weather data (`CONSULT`)
- Commands follow a line-based text protocol with `OK` / `ERR` responses.
- Successful logins, logouts, disconnects, and business operations are audited (US090).

### 1.3 Dependencies

- **US030:** Authentication and authorization.
- **US041:** Register weather data (domain rules reused).
- **US042:** Import bulk weather data.
- **US043:** Consult weather data.
- **US050:** Air Control Area registration.
- **US090:** External remote access logging (service identifier `US44`).

---

## 2. Analysis

US044 exposes weather functionality through a TCP-based remote access layer. The architecture separates generic session management from service-specific handlers:

```text
WeatherClientApp (TCP client)
        │
        ▼
RemoteAccessTcpServer
        │
        ▼
RemoteAccessClientHandler
        ├── LOGIN / LOGOUT / SELECT / BACK / HELP / QUIT
        └── WeatherRemoteHandler (when service WEATHER selected)
                ├── RegisterWeatherController   (US041)
                ├── ImportWeatherDataController (US042)
                └── ConsultWeatherDataController (US043)
```

Each TCP connection is handled in a dedicated thread. Bulk imports use chunked base64 transfer (`IMPORT_BEGIN` + chunks) with keep-alive messages during processing.

Audit events are sent via UDP to `RemoteAccessLoggingServer` using service identifier **`US44`**.

---

## 3. Design

### 3.1 Main Components

| Component | Package | Role |
|-----------|---------|------|
| `RemoteAccessTcpServer` | `presentation.network.remoteaccess` | Accepts TCP connections |
| `RemoteAccessClientHandler` | `presentation.network.remoteaccess` | Session, login, service selection |
| `WeatherRemoteHandler` | `presentation.network.remoteaccess` | Weather-specific commands |
| `WeatherClientApp` | `client.weather` | Interactive TCP client |
| `WeatherProtocol` | `presentation.network.weather` | Command constants and defaults |
| `RemoteAccessLogger` | `infrastructure.remoteaccess` | UDP audit logging (US090) |

### 3.2 Protocol Commands

Global commands (before or outside a service):

| Command | Description |
|---------|-------------|
| `HELLO` | Health check |
| `HELP` | List available commands |
| `LOGIN <user> <pass>` | Authenticate |
| `LOGOUT` | End session |
| `SELECT WEATHER` | Enter Weather Data service (US44) |
| `BACK` | Return to service selection |
| `QUIT` | Close connection |

Weather service commands (after `SELECT WEATHER`):

| Command | Maps to |
|---------|---------|
| `REGISTER ...` | US041 — register single weather record |
| `IMPORT ...` | US042 — import from file path on server |
| `IMPORT_BEGIN ...` | US042 — chunked base64 import |
| `CONSULT ...` | US043 — consult by area and date |

Default TCP port: **8044** (override with `-Dweather.server.port` or `WEATHER_SERVER_PORT`).

### 3.3 Session Flow

1. Client connects; server sends generic greeting (no service id before login).
2. Client sends `LOGIN username password`.
3. Server validates credentials and returns available services based on roles.
4. Client sends `SELECT WEATHER` (Weather Person or Admin).
5. Client issues weather commands; server delegates to application controllers.
6. Successful business operations trigger UDP audit events with identifier `US44`.
7. Client sends `LOGOUT` or `QUIT`; disconnect is logged.

---

## 4. Running

Start the server (embedded in `MainServer`):

```bash
./run-weather-server.sh [port]
```

Start the client:

```bash
./run-client.sh [host] [port]
```

Default connection: `127.0.0.1:8044`.

Example session:

```text
LOGIN weather Password1
SELECT WEATHER
REGISTER ACA01;2026-06-14;10:00;12:00;270;15;1013;65;3000
CONSULT ACA01;2026-06-14
LOGOUT
QUIT
```

---

## 5. Tests

- `WeatherRemoteAccessIT` — end-to-end TCP protocol, authentication, and audit logging
- `RemoteAccessLoggingTest` — UDP log format with service identifier `US44`

Run:

```bash
mvn test -Dtest=WeatherRemoteAccessIT
```

---

## 6. Related Stories

- **US041:** register weather data (business logic reused).
- **US042:** bulk import (including chunked transfer over TCP).
- **US043:** consult weather data by area and date.
- **US090:** external audit logging infrastructure.
- **US078 / US086:** same remote access framework with different service identifiers.
