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

// adicionar aqui as issues

---

## Auto-avaliação da Contribuição do Grupo

| Avaliador | Mariana Gabriel | Guilherme Tavares | Pedro Soares | Gonçalo Pinto | João Sousa |
|---|---|---|---|---|---|
| Mariana Gabriel | 0% | 0% | 0% | 0% | 0% |
| Guilherme Tavares | 0% | 0% | 0% | 0% | 0% |
| Pedro Soares | 0% | 0% | 0% | 0% | 0% |
| Gonçalo Pinto | 0% | 0% | 0% | 0% | 0% |
| João Sousa | 0% | 0% | 0% | 0% | 0% |