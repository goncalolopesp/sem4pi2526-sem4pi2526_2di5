# Flight Simulation System

## Índice

1. Introdução  
2. Arquitetura Geral  
3. Evolução da Arquitetura entre Sprints  
4. US100 - Simulate flights in a given area  
5. US102 - Detect safety violations  
6. US103 - Synchronize simulation steps  
7. US109 - Generate simulation report  
8. Estruturas Principais  
9. Decisões Técnicas  
10. Build e Execução  

---

# 1. Introdução

Este projeto implementa um sistema concorrente de simulação de voos utilizando processos POSIX, pipes, sinais e mecanismos de sincronização.

A solução foi desenvolvida em C e organizada de forma modular para suportar:
- simulação concorrente;
- monitorização de posições;
- sincronização temporal;
- deteção de violações de segurança;
- geração de relatórios.

A arquitetura foi evoluindo incrementalmente ao longo dos sprints, mantendo separação clara entre responsabilidades.

---

# 2. Arquitetura Geral

A solução encontra-se dividida nos seguintes componentes principais:

| Componente | Responsabilidade |
|---|---|
| `main.c` | Coordenação global da simulação |
| `flight_process.c` | Execução individual de cada voo |
| `flight_plan_parser.c` | Carregamento e parsing de planos de voo |
| `position_tracker.c` | Histórico temporal das posições |
| `safety_monitor.c` | Validação de segurança |
| `simulation.h` | Estruturas e contratos partilhados |

Cada voo executa num processo independente criado através de `fork()`.

A comunicação entre processos é realizada utilizando:
- pipes POSIX;
- sinais POSIX;
- sincronização passo-a-passo.

---

# 3. Evolução da Arquitetura entre Sprints

## Sprint 2

O objetivo inicial consistia em criar a base concorrente da simulação:
- criação de processos;
- comunicação por pipes;
- sinais POSIX;
- envio de posições;
- histórico temporal.

## Sprint 3

Durante o Sprint 3 foram introduzidas melhorias importantes:
- sincronização mais segura;
- suporte a múltiplos voos simultâneos;
- suporte a diferentes `DepartureTime`;
- melhor coordenação entre processos;
- melhoria da gestão de recursos;
- sincronização determinística passo-a-passo.

A arquitetura principal foi mantida, sendo introduzidas melhorias incrementais na robustez da implementação.

---

# 4. US100 - Simulate flights in a given area

## Objetivo

Simular voos concorrentes numa determinada área geográfica utilizando processos independentes.

## Responsabilidades do processo principal

- validar parâmetros;
- criar pipes;
- criar processos filho;
- coordenar os voos;
- iniciar os processos através de `SIGUSR2`;
- monitorizar múltiplos pipes com `poll()`;
- receber `PositionUpdate`;
- atualizar o histórico de posições;
- terminar processos corretamente;
- aguardar processos filho com `waitpid()`.

## Responsabilidades dos processos filho

- carregar o plano de voo;
- instalar signal handlers;
- aguardar sincronização;
- executar segmentos do voo;
- interpolar posições;
- enviar atualizações;
- terminar corretamente.

## Melhorias introduzidas no Sprint 3

### Substituição de `pause()` por `sigsuspend()`

A sincronização passou a utilizar:
- `sigprocmask()`
- `sigsuspend()`

Isto evita race conditions durante o arranque concorrente.

### Bloqueio explícito de sinais antes do `fork()`

O processo pai bloqueia `SIGUSR2` antes da criação dos filhos.

Os filhos herdam essa máscara, instalam os handlers necessários e apenas depois desbloqueiam temporariamente o sinal através de `sigsuspend()`.

### Alteração do sinal de arranque

O arranque da simulação passou a utilizar:
- `SIGUSR2` → arranque;
- `SIGUSR1` → reservado para violações de segurança.

### Gestão segura de runtime

Foi criada uma função dedicada para:
- fechar pipes;
- limpar `pollfd`;
- desativar voos;
- evitar fechos duplicados.

### Suporte a `DepartureTime`

Cada voo possui agora o seu próprio `DepartureTime`.

O processo principal apenas espera updates dos voos cuja hora de partida já foi atingida.

---

# 5. US102 - Detect safety violations

## Objetivo

Detetar violações básicas de segurança durante a simulação.

## Responsabilidades

- analisar posições recebidas;
- verificar separação mínima;
- validar altitude mínima;
- registar eventos de segurança;
- preparar integração futura com controlo de tráfego.

## Integração

O `SafetyMonitor` é executado pelo processo principal após receção dos updates de cada timestep.

---

# 6. US103 - Synchronize simulation steps

## Objetivo

Garantir sincronização temporal entre todos os voos da simulação.

## Modelo de sincronização

A implementação utiliza um modelo híbrido:

| Mecanismo | Responsabilidade |
|---|---|
| sinais POSIX | controlo do ciclo de vida |
| pipes | sincronização passo-a-passo |
| `poll()` | multiplexagem eficiente |

## Funcionamento

### Processo principal

O processo principal:
1. cria os processos;
2. envia o sinal inicial;
3. controla o timestep global;
4. autoriza os filhos a avançar;
5. recolhe updates sincronizados.

### Processos filho

Os processos filho:
1. aguardam sincronização;
2. executam apenas o timestep autorizado;
3. enviam posições;
4. aguardam nova autorização.

## Melhorias introduzidas

### Suporte a múltiplos voos concorrentes

A sincronização foi reorganizada para suportar:
- múltiplos pipes;
- múltiplos processos ativos;
- diferentes tempos de partida.

### Integração de `poll()`

O processo principal utiliza `poll()` para monitorizar múltiplos descritores sem busy waiting.

### Separação entre controlo e dados

- sinais → controlo;
- pipes → transmissão de dados.

Esta separação melhora clareza e robustez.

---

# 7. US109 - Generate simulation report

## Objetivo

Gerar um relatório simples da execução da simulação.

## Informação apresentada

- voos processados;
- posições recebidas;
- eventos de segurança;
- estados finais dos voos;
- resumo da execução.

---

# 8. Estruturas Principais

## `SimulationConfig`

Parâmetros globais da simulação:
- área;
- intervalo temporal;
- ficheiros de voo;
- thresholds de segurança.

## `FlightPlan`

Representação interna do plano de voo.

## `Segment`

Representa um segmento entre dois pontos.

## `PositionUpdate`

Mensagem enviada pelos filhos:
- posição;
- timestamp;
- altitude;
- velocidade;
- estado.

## `FlightRuntime`

Estado de runtime de cada voo:
- PID;
- pipes;
- estado ativo;
- `DepartureTime`.

## `HistoryEntry`

Entrada do histórico temporal.

## `PositionTracker`

Estrutura global de tracking das posições.

---

# 9. Decisões Técnicas

## Um processo por voo

Permite:
- isolamento;
- concorrência;
- melhor escalabilidade.

## Uso de pipes

Os pipes permitem comunicação simples e eficiente entre processos.

## Uso de `poll()`

`poll()` evita busy waiting e permite acompanhar múltiplos voos simultaneamente.

## Uso de sinais POSIX

Os sinais são utilizados apenas para:
- sincronização;
- arranque;
- terminação.

## Compatibilidade POSIX

Foi utilizada:
```c
#define _POSIX_C_SOURCE 200809L
````

para garantir compatibilidade com APIs POSIX.

---

# 10. Build e Execução

## Compilação

```bash
make
```

## Execução

```bash
./flight_simulator \
  --area 39 43 -9 -3 \
  --start 0 \
  --end 3600 \
  --step 60 \
  --flight data/Flight_Plan_v0a.json
```

## Exemplo com múltiplos voos

```bash
./flight_simulator \
  --area 39 43 -9 -3 \
  --start 0 \
  --end 3600 \
  --step 60 \
  --flight data/Flight1.json \
  --flight data/Flight2.json
```

## Diagrama de Componentes

```puml
@startuml

skinparam componentStyle rectangle

package "Flight Simulation System" {

    [main.c] as Main
    [flight_process.c] as FlightProcess
    [flight_plan_parser.c] as Parser
    [position_tracker.c] as Tracker
    [safety_monitor.c] as Safety
    [simulation.h] as Simulation

    database "Flight Plans JSON" as Json

    component "US100\nSimulate Flights" as US100
    component "US102\nSafety Validation" as US102
    component "US103\nStep Synchronization" as US103
    component "US109\nSimulation Report" as US109
}

Main --> FlightProcess : fork()
Main --> Tracker : update positions
Main --> Safety : validate positions
Main --> Parser : parse plans
Parser --> Json

FlightProcess --> Parser : load plan
FlightProcess --> Main : pipe(PositionUpdate)

US100 --> Main
US100 --> FlightProcess
US100 --> Parser

US102 --> Safety
US102 --> Tracker

US103 --> Main
US103 --> FlightProcess

US109 --> Tracker
US109 --> Safety
US109 --> Main

Simulation ..> Main
Simulation ..> FlightProcess
Simulation ..> Tracker
Simulation ..> Safety
Simulation ..> Parser

@enduml
````