# US100 - Simulate flights in a given area

## Objetivo

Implementação em C da US100. O componente cria um processo filho por voo, usa pipes para enviar atualizações de posição para o processo principal e usa sinais para controlar o início e a terminação da simulação.

## Relação com os critérios de aceitação

- Implementado em C.
- Usa `fork()` para criar um processo por voo.
- Usa `pipe()` para comunicação entre cada processo de voo e o processo principal.
- Usa sinais:
  - `SIGUSR1` inicia a execução dos processos de voo.
  - `SIGTERM`/`SIGINT` pedem terminação limpa.
- Cada processo de voo executa o seu plano de voo, calcula posições interpoladas nos segmentos e envia atualizações ao processo principal.
- O processo principal mantém histórico de posições por voo numa estrutura dinâmica (`HistoryEntry`).
- Os parâmetros obrigatórios são validados antes da execução, incluindo weather, safety thresholds e performance settings.

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
./us100_simulator --area 39 43 -9 -3 --start 0 --end 3600 --step 60 --weather clear --safety-min-sep 3 --safety-min-alt 300 --perf-max-flights 10 --flight data/Flight_Plan_v0a.json
```

Sem --start (usa DepartureTime do plano):
```bash
./us100_simulator --area 39 43 -9 -3 --end 3600 --step 60 --weather clear --safety-min-sep 3 --safety-min-alt 300 --perf-max-flights 10 --flight data/Flight_Plan_v0a.json
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

Durante a execução, o processo principal imprime as posições recebidas. No fim, é criado o ficheiro:

```text
simulation_report.txt
```

com o histórico de posições por voo.

## Nota de integração

Esta implementação foca a US100. A deteção de violações de segurança e o envio de notificações por `SIGUSR1` aos voos pertence à US102, e a sincronização forte por time step pertence à US103. A estrutura criada aqui já deixa a base preparada para essas US.
