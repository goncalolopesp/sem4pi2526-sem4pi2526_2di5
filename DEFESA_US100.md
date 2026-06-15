# Defesa Oral - US100

## 1. Objetivo da US

Permitir que um Operador de Controlo de Voo submeta uma simulação de voos numa
área. A aplicação EAPLI cria e acompanha a simulação, mas a execução concorrente
é feita pelo componente SCOMP em C.

Ideia principal para decorar:

> Java orquestra o caso de uso e persiste o estado; C executa a simulação com um
> processo por voo, pipes, sinais POSIX e sincronização por timestep.

## 2. Fluxo completo passo a passo

1. `RunAreaSimulationUI` recolhe área, intervalo temporal, meteorologia,
   limites de segurança e identificadores dos planos.
2. A UI chama `RunAreaSimulationController.createSimulation(...)`.
3. O controller autoriza `FLIGHT_CONTROL_OPERATOR` ou `ADMIN`.
4. `RunAreaSimulationService` cria o identificador e instancia `Simulation`.
5. Cada plano é adicionado através de `Simulation.addFlightPlan`, originando
   objetos `SimulatedFlightInput`.
6. `SimulationRepository.save` persiste o Aggregate Root com estado `CREATED`.
7. `SimulationDTORepresentationBuilder` cria o `SimulationDTO` devolvido à UI.
8. A UI chama `submitSimulation`.
9. `SimulationJobService` coloca o trabalho num `ExecutorService`, para a UI não
   ficar bloqueada.
10. O job volta a obter `Simulation` pelo repositório e muda o estado para
    `RUNNING`.
11. O job chama a abstração `SimulationRunner`.
12. `CFlightSimulatorRunner` traduz o agregado para argumentos do executável
    `scomp/flight_simulator`.
13. `ProcessBuilder` cria o processo C, junta stdout/stderr e espera pelo exit
    code.
14. Em C, `main` chama `parse_args` e `validate_config`.
15. O processo pai inicializa tracker, memória partilhada, log de segurança e
    threads auxiliares.
16. O pai bloqueia `SIGUSR2` antes do `fork`.
17. Para cada voo, cria um pipe e um processo filho.
18. Cada filho entra em `run_flight_process`, instala handlers, abre a memória
    partilhada, lê o JSON e espera com `sigsuspend`.
19. O pai repõe a máscara e envia `SIGUSR2` a todos os filhos.
20. Cada filho calcula posições por segmento e envia `PositionUpdate` pelo pipe.
21. O pai usa `poll` para esperar por vários pipes, guarda histórico e atualiza
    o `PositionTracker`.
22. O filho publica também a posição em memória partilhada para as extensões de
    segurança/sincronização de outras US.
23. O pai liberta o timestep seguinte através de `step_start[i]`.
24. No fim, cada filho envia `FINISHED` ou `ABORTED`, fecha o pipe e termina com
    `_exit`.
25. O pai usa `waitpid`, gera o relatório C, liberta recursos e termina.
26. `CFlightSimulatorRunner` converte exit code, caminho e consola em
    `SimulationExecutionResult`.
27. Quando há sucesso, o fluxo continua para a US111, que interpreta e persiste
    o relatório. Se falhar, a simulação fica `FAILED`.

## 3. Classes e ficheiros por ordem de execução

| Ordem | Elemento | Caminho completo no projeto | Responsabilidade |
|---|---|---|---|
| 1 | `RunAreaSimulationUI` | `src/main/java/pt/isep/sem4pi/presentation/simulation/RunAreaSimulationUI.java` | Recolher input e apresentar submissão/erro. |
| 2 | `RunAreaSimulationController` | `src/main/java/pt/isep/sem4pi/application/controller/simulation/RunAreaSimulationController.java` | Autorizar e coordenar o caso de uso. |
| 3 | `RunAreaSimulationService` | `src/main/java/pt/isep/sem4pi/application/service/simulation/RunAreaSimulationService.java` | Criar o agregado com os parâmetros recebidos. |
| 4 | `Simulation` | `src/main/java/pt/isep/sem4pi/domain/model/simulation/Simulation.java` | Aggregate Root da simulação e respetivas transições. |
| 5 | `SimulatedFlightInput` | `src/main/java/pt/isep/sem4pi/domain/model/simulation/SimulatedFlightInput.java` | Value Object que identifica um plano incluído. |
| 6 | `SimulationRepository` | `src/main/java/pt/isep/sem4pi/domain/repositories/SimulationRepository.java` | Contrato de persistência orientado ao domínio. |
| 7 | `JpaSimulationRepository` | `src/main/java/pt/isep/sem4pi/infrastructure/persistence/JPA/JpaSimulationRepository.java` | Implementação JPA do repositório. |
| 8 | `SimulationDTORepresentationBuilder` | `src/main/java/pt/isep/sem4pi/application/viadto/SimulationDTORepresentationBuilder.java` | Mapear o agregado para DTO. |
| 9 | `SimulationDTO` | `src/main/java/pt/isep/sem4pi/domain/model/simulation/DTO/SimulationDTO.java` | Transportar dados para a apresentação. |
| 10 | `SimulationJobService` | `src/main/java/pt/isep/sem4pi/application/service/simulation/SimulationJobService.java` | Executar a simulação assincronamente. |
| 11 | `SimulationRunner` | `src/main/java/pt/isep/sem4pi/application/service/simulation/SimulationRunner.java` | Contrato para qualquer motor de simulação. |
| 12 | `CFlightSimulatorRunner` | `src/main/java/pt/isep/sem4pi/infrastructure/simulation/CFlightSimulatorRunner.java` | Adaptar Java ao executável C. |
| 13 | `SimulationExecutionResult` | `src/main/java/pt/isep/sem4pi/application/service/simulation/SimulationExecutionResult.java` | Transportar exit code lógico, caminho e output. |
| 14 | `Makefile` | `scomp/Makefile` | Compilar e ligar o simulador C. |
| 15 | `main` | `scomp/src/main.c` | Coordenar processos, pipes, passos e relatório. |
| 16 | estruturas/contratos C | `scomp/src/simulation.h` | Definir mensagens, configuração e estado partilhado. |
| 17 | parser | `scomp/src/flight_plan_parser.c` | Ler o plano JSON para estruturas C. |
| 18 | processo de voo | `scomp/src/flight_process.c` | Simular um voo e enviar posições. |
| 19 | tracker | `scomp/src/position_tracker.c` | Guardar estado atual e histórico. |
| 20 | memória partilhada | `scomp/src/shared_simulation.c` | Criar/destruir memória e primitivas partilhadas. |
| 21 | segurança | `scomp/src/safety_monitor.c` | Detetar e registar violações, dependência de outras US. |
| 22 | threads auxiliares | `scomp/src/simulation_threads.c` | Deteção e log concorrente, extensão de outras US. |

`FlightPlanJsonExporter` não pertence ao fluxo direto desta US: é usado na
preparação/teste de planos noutra funcionalidade. A US100 recebe caminhos de
ficheiros já disponíveis em `scomp/data`.

## 4. Porque existe e porque foi feito assim

### `RunAreaSimulationController`

Existe para ser a porta de entrada do caso de uso. Autoriza, chama serviços,
persiste e devolve uma representação.

Foi implementado assim para manter a UI sem regras e sem acesso direto à
persistência. Não foi colocado tudo na UI porque isso criaria forte acoplamento
entre consola, domínio, JPA e SCOMP.

Princípios/padrões: Controller, Service Layer, Low Coupling.

**Pergunta:** Porque existe controller se a UI podia chamar o serviço?

**Resposta curta:** Para centralizar autorização e coordenação do caso de uso.

**Resposta completa:** A UI só trata interação. O controller define a fronteira
da aplicação, verifica perfis e coordena serviço, repositório, DTO e submissão.
Isso permite trocar a consola por outra interface sem mover regras.

### `Simulation`

Existe porque a simulação tem identidade, ciclo de vida e invariantes próprias.
É Aggregate Root porque controla os `SimulatedFlightInput` e as transições de
estado.

Foi implementada com métodos como `markAsRunning` em vez de setters para impedir
estados inválidos. Não se altera `status` diretamente porque isso espalharia a
máquina de estados pelas camadas.

Princípios/padrões: DDD Aggregate Root, Information Expert, High Cohesion.

**Pergunta:** Porque é `Simulation` Aggregate Root?

**Resposta curta:** Porque é a única entrada para alterar a simulação e os seus
objetos internos.

**Resposta completa:** Tem identidade `simulationId`, é persistida como unidade,
controla a coleção de planos e protege transições `CREATED -> RUNNING ->
FINISHED/FAILED`. Os objetos internos não têm repositório próprio.

### `SimulatedFlightInput`

Existe para dar significado de domínio ao identificador do plano usado na
simulação.

Foi implementado como `@Embeddable`, com igualdade por valor, porque não tem
ciclo de vida nem identidade independente. Não é Entity porque só existe no
contexto de `Simulation`.

Princípios/padrões: Value Object, composição.

**Pergunta:** Porque não guardar apenas `Set<String>`?

**Resposta curta:** O Value Object dá significado, validação e espaço de
evolução ao conceito.

**Resposta completa:** Uma `String` não expressa intenção. O objeto valida o
valor, define igualdade e permite acrescentar comportamento sem contaminar o
Aggregate Root.

### `SimulationRepository`

Existe para obter e guardar aggregates sem expor JPA.

Foi implementado como interface no domínio e implementação na infraestrutura.
Não foi usado `EntityManager` no controller porque isso violaria a separação de
camadas e dificultaria testes.

Princípios/padrões: Repository, Dependency Inversion, Protected Variations.

**Pergunta:** O repository guarda tabelas?

**Resposta curta:** Não; conceptualmente guarda e recupera aggregates.

**Resposta completa:** A implementação JPA usa tabelas, mas o contrato fala em
`Simulation`. A aplicação depende da linguagem do domínio, não da tecnologia.

### DTO e Mapper

`SimulationDTO` existe para transportar dados sem expor a entidade JPA.
`SimulationDTORepresentationBuilder` concentra a conversão.

Não se devolve o Aggregate Root à UI porque esta poderia ficar acoplada ao
modelo persistente, a lazy loading e a métodos de mutação.

Princípios/padrões: DTO, Mapper/Representation Builder, Low Coupling.

**Pergunta:** DTO é um objeto de domínio?

**Resposta curta:** Não; é um objeto de transporte sem regras de negócio.

**Resposta completa:** O DTO é moldado para a fronteira da aplicação. O domínio
mantém invariantes; o DTO apenas comunica dados.

### `SimulationJobService`

Existe porque a simulação pode demorar e não deve bloquear a consola.

Foi implementado com `ExecutorService` e recarrega o agregado pelo ID dentro do
job. Não transporta a entidade recebida pela UI para outra thread, evitando
usar uma instância potencialmente destacada ou desatualizada.

Princípios/padrões: Application Service, assíncrono, Low Coupling.

**Pergunta:** Porque recarregar a simulação?

**Resposta curta:** Porque o job executa mais tarde e precisa do estado
persistido atual.

**Resposta completa:** A passagem do ID define uma fronteira limpa. A nova
thread abre o seu fluxo, consulta o repositório e trabalha sobre a versão que a
persistência reconhece.

### `SimulationRunner` e `CFlightSimulatorRunner`

`SimulationRunner` existe para proteger a aplicação da variação do motor.
`CFlightSimulatorRunner` é o adaptador concreto para SCOMP.

Foi usado `ProcessBuilder` porque a integração é entre processos e o binário C
já é um componente autónomo. Não foi usado JNI porque aumentaria acoplamento,
complexidade de memória e dependência da plataforma dentro da JVM.

Princípios/padrões: Adapter, Dependency Inversion, Protected Variations.

**Pergunta:** Onde está a chamada de Java para C?

**Resposta curta:** Em `CFlightSimulatorRunner`, através de `ProcessBuilder`.

**Resposta completa:** O runner constrói a linha de comandos, inicia
`scomp/flight_simulator`, lê stdout/stderr, espera pelo processo e devolve um
resultado neutro à aplicação.

### `main.c`

Existe como coordenador do simulador. Valida configuração, cria recursos,
lança filhos, recolhe updates, sincroniza passos e fecha tudo.

Foi mantido um processo pai central para existir uma visão global. Não se deixa
cada filho gerar o relatório porque nenhum filho conhece todos os voos.

Princípios: Controller/Coordinator no contexto C, High Cohesion.

**Pergunta:** Porque um processo por voo?

**Resposta curta:** É requisito da US e fornece isolamento concorrente.

**Resposta completa:** Cada voo possui execução e sinais próprios. Uma falha ou
terminação é identificável por PID, e o pai mantém a visão global através de
IPC.

### Pipes, `poll`, sinais e `waitpid`

- Pipe: canal filho -> pai para `PositionUpdate`.
- Um pipe por voo: isolamento e associação direta ao runtime.
- `poll`: espera eficiente por vários descritores.
- `SIGUSR2`: arranque.
- `SIGUSR1`: notificação de violação.
- `SIGTERM`/`SIGINT`: término controlado.
- `waitpid`: recolhe filhos e evita zombies.

**Pergunta:** Porque não memória partilhada para todos os updates?

**Resposta curta:** O pipe é adequado ao fluxo ordenado filho -> pai e simplifica
ownership.

**Resposta completa:** A mensagem é pequena, sequencial e tem um único
consumidor. A memória partilhada exige sincronização adicional. Ela existe para
extensões concorrentes, mas o contrato base da US100 continua explícito no pipe.

### `sigsuspend` e máscara antes do `fork`

O pai bloqueia `SIGUSR2` antes de criar filhos. Os filhos herdam a máscara,
instalam o handler e esperam com `sigsuspend`, que desbloqueia temporariamente o
sinal de forma atómica.

**Pergunta:** Porque não `pause()`?

**Resposta curta:** `pause()` permite perder o sinal entre testar a flag e
começar a esperar.

**Resposta completa:** Com máscara herdada mais `sigsuspend`, o sinal não pode
ser tratado cedo demais. Elimina-se a race condition clássica de sinais.

### `PositionUpdate`, `FlightRuntime` e `PositionTracker`

`PositionUpdate` é a mensagem IPC. `FlightRuntime` guarda recursos e histórico
do ponto de vista do pai. `PositionTracker` fornece a visão atual e histórica
orientada ao acompanhamento.

Não são enviados ponteiros no pipe porque endereços virtuais não representam
objetos partilháveis entre processos.

## 5. Camadas EAPLI

| Camada | Responsabilidade nesta US |
|---|---|
| Apresentação | Ler input e mostrar resultado: `RunAreaSimulationUI`. |
| Aplicação/controller | Autorizar e coordenar: controller e services. |
| Domínio | `Simulation`, estados, invariantes e Value Objects. |
| Persistência | Interfaces Repository no domínio; JPA na infraestrutura. |
| Infraestrutura | Executar processo C e configurar persistência. |
| SCOMP/C | Concorrência, IPC, movimento, histórico e ficheiro técnico. |

## 6. Conceitos EAPLI essenciais

### Aggregate Root

Entidade que define a fronteira de consistência do agregado. Só ela deve ser
obtida pelo repositório e usada para alterar objetos internos. Aqui:
`Simulation`.

### Value Object

Objeto sem identidade própria, comparado pelo valor e normalmente imutável.
Aqui: `SimulatedFlightInput`.

### Repository

Abstração semelhante a uma coleção de aggregates. Aqui:
`SimulationRepository`.

### DTO e Mapper

DTO transporta dados; Mapper converte domínio para DTO. Evitam expor a entidade
à UI.

### Service e Controller

Controller representa a operação do ator. Service executa coordenação ou lógica
de aplicação que não deve ficar na UI nem numa entidade.

### Factory

`PersistenceContext` seleciona uma `RepositoryFactory`; a factory cria
repositórios concretos. A escolha de JPA ou memória fica fora do domínio.

### `TransactionalContext`

Não é o mecanismo central da criação inicial desta US, que usa o repositório
auto-transacional. Torna-se essencial no fecho US111, quando relatório e estado
da simulação têm de ser gravados atomicamente.

### `@Version` e Optimistic Locking

`Simulation.version` permite ao JPA detetar se duas operações carregaram a mesma
versão e tentaram gravar alterações incompatíveis. Não bloqueia a linha durante
todo o trabalho; verifica a versão no update.

### Protected Variations

Identificar pontos que podem mudar e protegê-los com interfaces. Exemplo:
`SimulationRunner` protege a aplicação da forma concreta de executar o motor.

### High Cohesion e Low Coupling

High Cohesion: cada módulo concentra uma responsabilidade relacionada.
Low Coupling: UI, domínio, JPA e C comunicam por contratos estreitos.

## 7. Perguntas que quase de certeza vão aparecer

1. **Onde é feita a passagem Java -> C?**  
   Em `CFlightSimulatorRunner`, com `ProcessBuilder`.

2. **Porque não implementar a simulação em Java?**  
   O requisito SCOMP exige C e mecanismos POSIX; Java fica responsável pelo caso
   de uso EAPLI.

3. **Porque existe `SimulationRunner`?**  
   Para aplicar Protected Variations e permitir outro motor/fake em testes.

4. **Porque a execução é assíncrona?**  
   Para não bloquear a UI durante um processo externo potencialmente longo.

5. **Porque um processo por voo?**  
   Requisito, isolamento, PID próprio e concorrência explícita.

6. **Como comunicam filho e pai?**  
   Cada filho escreve estruturas `PositionUpdate` no seu pipe.

7. **Porque `poll`?**  
   Para monitorizar vários pipes de forma eficiente.

8. **Como evitam perder o sinal de arranque?**  
   Bloqueiam `SIGUSR2` antes do `fork` e esperam com `sigsuspend`.

9. **Quem controla o estado da simulação?**  
   O Aggregate Root `Simulation`.

10. **Para que serve `@Version`?**  
    Para Optimistic Locking e deteção de updates concorrentes.

## 8. Perguntas difíceis

### O C devolve o relatório diretamente como objeto Java?

Não. O contrato técnico tem duas partes: exit code/output do processo e um
ficheiro. Java recebe `SimulationExecutionResult`; a US111 interpreta o ficheiro.
Isto mantém os processos desacoplados.

### O pipe garante que uma estrutura chega inteira?

Para escritas de tamanho inferior a `PIPE_BUF`, uma escrita é atómica. O código
escreve uma estrutura pequena numa única chamada. O pai confirma que leu
`sizeof(PositionUpdate)`.

### Porque existem pipe e memória partilhada?

O pipe suporta o fluxo base filho -> pai da US100. A memória partilhada suporta
extensões de sincronização e deteção concorrente de outras US. Têm consumidores
e responsabilidades diferentes.

### O que acontece se o processo C for interrompido?

O runner repõe a flag de interrupção Java em `InterruptedException`. Em C, o pai
marca término, envia `SIGTERM`, desbloqueia semáforos, usa `waitpid` e liberta
recursos.

### Há um ponto crítico de concorrência entre UI e job?

Sim. A UI cria/submete e o job muda estados. `@Version` existe precisamente para
detetar escritas concorrentes sobre uma versão antiga do agregado.

### Porque não há uma grande transação durante a execução C?

Uma transação de BD não deve ficar aberta enquanto um processo externo demora.
A simulação é um workflow assíncrono: persistem-se estados curtos antes/depois,
e a conclusão conjunta com o relatório é tratada na US111.

## 9. Folha A4 - revisão rápida

### Frase de abertura

US100 separa orquestração EAPLI em Java da simulação concorrente em C.

### Fluxo

`UI -> Controller -> Service -> Simulation -> Repository -> DTO -> Job ->
SimulationRunner -> CFlightSimulatorRunner -> ProcessBuilder -> main.c ->
fork/pipe/SIGUSR2/poll -> relatório -> Java`

### Decorar

- `Simulation`: Aggregate Root.
- `SimulatedFlightInput`: Value Object.
- `SimulationRepository`: abstrai JPA.
- `SimulationDTO`: protege domínio da UI.
- `SimulationJobService`: execução assíncrona.
- `SimulationRunner`: Protected Variations.
- `CFlightSimulatorRunner`: Adapter Java/C.
- `ProcessBuilder`: cria o processo C.
- `fork`: um filho por voo.
- pipe: `PositionUpdate`, filho -> pai.
- `poll`: vários pipes.
- `SIGUSR2`: arranque.
- `SIGUSR1`: violação.
- `SIGTERM`: terminar.
- máscara antes do `fork` + `sigsuspend`: evita perder sinal.
- `waitpid`: evita zombies.
- `@Version`: Optimistic Locking.

### Resposta de 20 segundos

O operador cria uma `Simulation`, Aggregate Root persistido por repository. A
execução é submetida assincronamente. A aplicação depende de `SimulationRunner`
e a infraestrutura concretiza-o com `CFlightSimulatorRunner`, que usa
`ProcessBuilder`. Em C, o pai cria um processo e um pipe por voo, inicia-os com
`SIGUSR2`, recolhe `PositionUpdate` com `poll`, sincroniza timesteps, espera com
`waitpid` e gera o relatório consumido pela US111.
