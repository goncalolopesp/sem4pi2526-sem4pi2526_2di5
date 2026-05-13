# US103 - Synchronized flight simulation

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
./us103_simulator --area 39 43 -9 -3 --start 0 --end 3600 --step 60 --weather clear --safety-min-sep 3 --safety-min-alt 300 --perf-max-flights 10 --flight data/Flight_Plan_v0a.json
```

Sem `--start` (usa `DepartureTime` do plano):

```bash
./us103_simulator --area 39 43 -9 -3 --end 3600 --step 60 --weather clear --safety-min-sep 3 --safety-min-alt 300 --perf-max-flights 10 --flight data/Flight_Plan_v0a.json
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