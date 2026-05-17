# SCOMP – Sprint 3

## User Stories Implementadas

### US100 – Simulação Base de Voos

#### Abordagem Seguida

A solução foi implementada em C utilizando mecanismos POSIX de gestão de processos e comunicação interprocessos.

O processo principal:
- valida os parâmetros da simulação;
- cria um pipe e um processo filho por cada voo usando `fork()`;
- inicia todas as simulações através de `SIGUSR1`;
- recebe atualizações de posição dos voos usando `poll()`;
- guarda o histórico recebido em memória;
- gera um relatório final da simulação.

Cada processo filho:
- lê o ficheiro do plano de voo;
- espera pelo sinal de início;
- simula o percurso segmento a segmento;
- calcula posições intermédias através de interpolação linear;
- envia atualizações para o processo principal através do pipe.

A comunicação entre processos é feita através de estruturas binárias (`struct PositionUpdate`) em vez de mensagens de texto.

#### Principais Decisões Técnicas

- Utilização de `fork()` para criar um processo independente por avião.
- Utilização de pipes POSIX para comunicação unidirecional entre processos.
- Utilização de `poll()` para monitorizar múltiplos pipes sem bloqueios.
- Utilização de sinais UNIX (`SIGUSR1`, `SIGTERM`, `SIGINT`) para controlo dos processos.
- Utilização de tempo lógico de simulação em vez de tempo real.
- Implementação de um parser simples adaptado ao formato fornecido dos planos de voo.

#### Estrutura Principal do Código

- `main.c`
  - controlo principal da simulação;
  - validação de parâmetros;
  - criação de processos;
  - gestão da comunicação;
  - geração do relatório final.

- `flight_process.c`
  - lógica de execução dos voos;
  - tratamento de sinais;
  - interpolação das posições;
  - envio de atualizações.

- `flight_plan_parser.c`
  - leitura e parsing dos ficheiros de plano de voo.

- `simulation.h`
  - estruturas partilhadas;
  - constantes;
  - declarações de funções.

#### Implementado com Sucesso

- Arquitetura com um processo por voo;
- Comunicação entre processos através de pipes;
- Sincronização com sinais;
- Execução concorrente de múltiplos voos;
- Simulação baseada em tempo lógico;
- Histórico de posições;
- Geração de relatório final;
- Alocação dinâmica do histórico;
- Validação dos parâmetros da simulação.

#### Não Implementado

- Física real de voo;
- Simulação de vento ou combustível;
- Deteção de colisões;
- Esperas em tempo real;
- Parser JSON genérico;
- Comunicação através de memória partilhada;
- Simulação baseada em threads.

#### Limitações Conhecidas

- O parser apenas suporta a estrutura dos ficheiros fornecidos.
- O movimento dos aviões é simplificado através de interpolação linear.
- A simulação utiliza apenas tempo lógico.
- `poll()` é suficiente para a escala atual, mas poderia necessitar de otimizações para um número muito elevado de voos.

---

## Compilação e Execução

### Compilar

```bash
make
```

## Executar

```bash
make run
```

Ou manualmente:

```bash
./flight_simulator --area 39 43 -9 -3 --start 0 --end 3600 --step 60 --weather clear --safety-min-sep 3 --safety-min-alt 300 --perf-max-flights 10 --flight data/Flight_Plan_v0a.json
```

Sem --start (usa DepartureTime do plano):
```bash
./flight_simulator --area 39 43 -9 -3 --end 3600 --step 60 --weather clear --safety-min-sep 3 --safety-min-alt 300 --perf-max-flights 10 --flight data/Flight_Plan_v0a.json
```

## Parâmetros

```bash
--area <minLat> <maxLat> <minLon> <maxLon>
--start <segundos>   (opcional se existir DepartureTime no plano)
--end <segundos>     (se --start for omitido, é tratado como duração)
--step <segundos>
--weather <clear|rain|storm|fog>
--safety-min-sep <nm>
--safety-min-alt <m>
--perf-max-flights <n>
--flight <ficheiro>
```

É possível indicar vários `--flight`, sendo criado um processo para cada ficheiro (limitado por `--perf-max-flights`).

### Limpar

```bash
make clean
```

---

## Output

A simulação gera:
- atualizações no terminal durante a execução;
- um ficheiro `simulation_report.txt` contendo:
  - informação dos voos;
  - número de updates recebidos;
  - posições ao longo do tempo;
  - estado final de cada voo.

---

### US103 - Synchronized flight simulation

## Objetivo

Implementação em C da US103. O componente sincroniza a simulação de múltiplos voos através de processos, pipes e sinais UNIX, garantindo que todos os voos avançam de forma coordenada por time steps.

Cada processo filho executa um voo e envia uma atualização de posição ao processo principal. O processo principal apenas permite o avanço para o próximo passo depois de receber os updates de todos os voos ativos.

## Relação com os critérios de aceitação

- Implementado em C.
- Usa `fork()` para criar um processo por voo.
- Usa `pipe()` para comunicação entre cada processo de voo e o processo principal.
- Usa sinais:
  - `SIGUSR1` inicia a execução sincronizada dos voos.
  - `SIGUSR2` autoriza os processos filhos a avançar para o próximo time step.
  - `SIGTERM`/`SIGINT` pedem terminação limpa.
- Cada processo de voo executa o seu plano de voo e envia uma atualização por ciclo.
- O processo principal mantém histórico de posições por voo numa estrutura dinâmica (`HistoryEntry`).
- O processo principal sincroniza todos os voos antes de avançar a simulação.
- Os parâmetros obrigatórios são validados antes da execução, incluindo weather, safety thresholds e performance settings.

## Funcionamento da sincronização

Fluxo simplificado da simulação:

```text
Processo principal cria os processos filhos
↓
SIGUSR1 inicia todos os voos
↓
Cada voo envia um PositionUpdate
↓
Cada voo fica bloqueado à espera de SIGUSR2
↓
O processo principal recebe os updates de todos os voos
↓
O processo principal envia SIGUSR2
↓
Todos os voos avançam para o próximo time step
````

A sincronização é feita utilizando `poll()` no processo principal e `pause()` nos processos filhos.

## Dados usados

O ficheiro `data/Flight_Plan_v0a.json` foi incluído como exemplo. O parser é simples e foi feito para suportar o formato fornecido: lê `ID`, `Segments`, `Mode`, `Start`, `End`, `Latitude`, `Longitude` e altitude em metros.

## Compilar

```bash
make
```

## Executar exemplo

```bash
make run
```

Ou manualmente:

```bash
./flight_simulator --area 39 43 -9 -3 --start 0 --end 3600 --step 60 --weather clear --safety-min-sep 3 --safety-min-alt 300 --perf-max-flights 10 --flight data/Flight_Plan_v0a.json
```

Sem `--start` (usa `DepartureTime` do plano):

```bash
./flight_simulator --area 39 43 -9 -3 --end 3600 --step 60 --weather clear --safety-min-sep 3 --safety-min-alt 300 --perf-max-flights 10 --flight data/Flight_Plan_v0a.json
```

## Parâmetros

```bash
--area <minLat> <maxLat> <minLon> <maxLon>
--start <segundos>   (opcional se existir DepartureTime no plano)
--end <segundos>     (se --start for omitido, é tratado como duração)
--step <segundos>
--weather <clear|rain|storm|fog>
--safety-min-sep <nm>
--safety-min-alt <m>
--perf-max-flights <n>
--flight <ficheiro>
```

É possível indicar vários `--flight`, sendo criado um processo para cada ficheiro (limitado por `--perf-max-flights`).

## Output

Durante a execução, o processo principal imprime as posições recebidas por step sincronizado.

No fim, é criado o ficheiro:

```text
simulation_report.txt
```

com o histórico completo de posições por voo.

## Nota de integração

Esta implementação foca a sincronização temporal da simulação da US103. A deteção de conflitos entre aeronaves e alterações automáticas de voo pertence à US102.

```

---

## Auto-avaliação da Contribuição do Grupo

| Avaliador | Mariana Gabriel | Guilherme Tavares | Pedro Soares | Gonçalo Pinto | João Sousa |
|---|---|---|---|---|---|
| Mariana Gabriel | 100% | 100% | 0% | 0% | 0% |
| Guilherme Tavares | 100% | 100% | 0% | 0% | 0% |
| Pedro Soares | 0% | 0% | 0% | 0% | 0% |
| Gonçalo Pinto | 0% | 0% | 0% | 0% | 0% |
| João Sousa | 0% | 0% | 0% | 0% | 0% |