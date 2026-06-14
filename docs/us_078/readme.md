# US078 — Air Transport Company Collaborator Remote Access

## 1. User Story

As an Air Transport Company Collaborator, I want to remotely access the system using the Air Transport Company App.

---

## 2. Acceptance Criteria

| Critério | Estado |
|----------|--------|
| Cliente TCP dedicado | Implementado (`AirTransportCompanyClientApp`) |
| Interação limitada a TCP (sem acesso directo à BD no cliente) | Implementado |
| Operações ATCC disponíveis remotamente | Implementado (US070–US074) |
| Autenticação e autorização | Implementado (role `AIR_TRANSPORT_COMPANY_COLLABORATOR` ou `ADMIN`) |
| Integração com US090 (logging remoto) | Implementado (identificador de serviço `US78`) |

**Nota:** As user stories de pilotos (US075, US076, US077) não estão incluídas nesta implementação porque ainda não existem no projecto.

---

## 3. User Stories expostas remotamente

| US | Operação | Comando TCP |
|----|----------|-------------|
| US070 | Add aircraft to fleet | `ADD_AIRCRAFT` |
| US071 | Decommission aircraft | `RETIRE_AIRCRAFT` |
| US072 | List company fleet | `LIST_FLEET` |
| US072a–d | List fleet by sort option | `LIST_FLEET <sort>` |
| US073 | Create flight route | `CREATE_ROUTE` |
| US074 | Deactivate flight route | `DEACTIVATE_ROUTE` |

Opções de ordenação para `LIST_FLEET`: `REGISTRATION`, `MAKER`, `MODEL`, `CAPACITY`, `AGE`.

---

## 4. Arquitectura

A US78 partilha o **servidor TCP unificado** com a US44, mas está **separada** a nível de serviço, handler e cliente.

```text
AirTransportCompanyClientApp (US78)
        │
        │  TCP (porta 8044 por defeito)
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
   Persistência (servidor)
```

A US44 (`WeatherClientApp` + `WeatherRemoteHandler`) permanece independente e usa `SELECT WEATHER`.

Componentes principais:

| Componente | Pacote / ficheiro |
|------------|-------------------|
| Cliente | `pt.isep.sem4pi.client.airtransportcompany.AirTransportCompanyClientApp` |
| Handler servidor | `pt.isep.sem4pi.presentation.network.remoteaccess.AirTransportCompanyRemoteHandler` |
| Registo de serviço | `RemoteAccessService.ATCC` (`US78`) |
| Servidor TCP | `RemoteAccessTcpServer` / `MainServer` |

---

## 5. Protocolo TCP

### 5.1 Sessão

1. Cliente liga ao servidor.
2. `LOGIN <username> <password>`
3. Servidor responde com roles e menu de serviços.
4. `SELECT ATCC`
5. Comandos de negócio ATCC.
6. `BACK` — volta ao menu de serviços.
7. `LOGOUT` / `QUIT`

Respostas usam prefixos `OK` ou `ERR` (definidos em `RemoteAccessProtocol`).

### 5.2 Comandos ATCC

```text
ADD_AIRCRAFT <companyIata> <registration> <manufacturer> <model> <country> <eco> <bus> <first>
RETIRE_AIRCRAFT <registration>
LIST_FLEET [REGISTRATION|MAKER|MODEL|CAPACITY|AGE]
CREATE_ROUTE <routeId> <companyIata> <originIcao> <destinationIcao>
DEACTIVATE_ROUTE <routeId> <yyyy-MM-dd>
HELP
BACK
```

### 5.3 Exemplo — listar frota

**Cliente envia:**
```text
LIST_FLEET REGISTRATION
```

**Servidor responde:**
```text
OK 2
CS-TST;A320;Airbus;ACTIVE;180;5
CS-RET;B737;Boeing;ACTIVE;160;8
```

Cada linha de frota usa `;` como separador: registo, modelo, fabricante, estado, capacidade, idade.

---

## 6. Autenticação e autorização

- Login validado pelo `AuthenticationController` no servidor.
- Cada operação ATCC exige role `AIR_TRANSPORT_COMPANY_COLLABORATOR` ou `ADMIN`.
- O cliente **não** acede a repositórios nem à base de dados; toda a lógica corre no servidor após autenticação TCP.
- Utilizador de teste (bootstrap): `atccollaborator` / `Password1`

---

## 7. Execução

### Servidor (partilhado US44 + US78)

```bash
./run-weather-server.sh
# ou: ./run-weather-server.sh 8044
```

### Cliente US78

```bash
./run-atcc-client.sh
# ou: ./run-atcc-client.sh 127.0.0.1 8044
```

Variáveis de ambiente opcionais para a porta do servidor:

- `REMOTE_ACCESS_SERVER_PORT`
- `WEATHER_SERVER_PORT` (compatibilidade)

---

## 8. Testes

Testes de integração TCP:

- `src/test/java/pt/isep/sem4pi/presentation/network/remoteaccess/AirTransportCompanyRemoteAccessIT.java`

Cobrem autenticação, selecção de serviço, autorização por role e listagem de frota.

Executar:

```bash
mvn test
```

---

## 9. Dependências

Esta US depende de:

- US030 — autenticação e autorização
- US070–US074 — controllers de negócio já implementados
- US044 — infraestrutura TCP unificada (`development`)
- US090 — logging remoto (identificador `US78`)

---

## 10. Limitações conhecidas

- US075, US076 e US077 (gestão de pilotos) **não implementadas** — fora de scope até existirem controllers de domínio.
- O cliente US78 e o cliente US44 ligam-se ao **mesmo servidor TCP**; a separação é feita por serviço (`SELECT ATCC` vs `SELECT WEATHER`), não por portas distintas.
