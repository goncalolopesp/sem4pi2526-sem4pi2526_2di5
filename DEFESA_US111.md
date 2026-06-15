# Defesa Oral - US111

## 1. Objetivo da US

Gerar um resumo persistente e legível dos resultados de uma simulação:

- total de voos;
- estado de execução de cada voo;
- instante e posição das violações;
- decisão final `PASS` ou `FAIL`;
- localização do ficheiro gerado.

A US111 não volta a simular. Consome o relatório produzido pelo SCOMP.

## 2. Fluxo completo passo a passo

1. `SimulationJobService` recebe o resultado bem-sucedido do SCOMP.
2. Chama `GenerateSimulationSummaryReportService.generate`.
3. `SimulationReportParser` lê o ficheiro técnico do SCOMP.
4. O parser extrai posições, estados, violações, total e resultado final.
5. Valida se o total declarado coincide com os estados e se todas as violações
   estão listadas.
6. Produz `ParsedSimulationReport`.
7. O serviço cria `FlightExecutionSummary`, `SafetyViolationSummary`,
   `ReportFilePath` e o Aggregate Root `SimulationReport`.
8. `SimulationReport` recalcula a decisão final:
   - SCOMP `FAIL` -> `FAIL`;
   - violações -> `FAIL`;
   - voo não `FINISHED` -> `FAIL`;
   - caso contrário -> `PASS`.
9. `SimulationSummaryReportExporter` escreve primeiro um ficheiro temporário e
   move-o para o destino.
10. O serviço abre um `TransactionalContext`.
11. Obtém `SimulationRepository` e `SimulationReportRepository` associados à
    mesma transação.
12. Guarda o relatório.
13. Atualiza `Simulation` para `FINISHED` ou `FAILED`.
14. Guarda a simulação e faz `commit`.
15. Se a persistência falhar, faz `rollback` e elimina o ficheiro como
    compensação.
16. Para consulta, `ViewSimulationReportUI` chama
    `CheckSimulationStatusController`.
17. O controller obtém o agregado pelo repositório.
18. `SimulationSummaryReportDTOMapper` converte-o em
    `SimulationSummaryReportDTO`.
19. A UI apresenta o resumo sem receber entidades JPA.

## 3. Classes e caminhos por ordem

| Elemento | Caminho | Papel |
|---|---|---|
| `SimulationJobService` | `src/main/java/pt/isep/sem4pi/application/service/simulation/SimulationJobService.java` | Dispara a geração após o SCOMP. |
| `GenerateSimulationSummaryReportService` | `src/main/java/pt/isep/sem4pi/application/service/simulation/GenerateSimulationSummaryReportService.java` | Orquestra o caso de uso e a transação. |
| `SimulationReportParser` | `src/main/java/pt/isep/sem4pi/application/service/simulation/SimulationReportParser.java` | Traduz o formato SCOMP. |
| `ParsedSimulationReport` | `src/main/java/pt/isep/sem4pi/application/service/simulation/ParsedSimulationReport.java` | Resultado imutável do parser. |
| `SimulationReport` | `src/main/java/pt/isep/sem4pi/domain/model/simulation/SimulationReport.java` | Aggregate Root do relatório. |
| `FlightExecutionSummary` | `src/main/java/pt/isep/sem4pi/domain/model/simulation/FlightExecutionSummary.java` | Value Object de execução. |
| `SafetyViolationSummary` | `src/main/java/pt/isep/sem4pi/domain/model/simulation/SafetyViolationSummary.java` | Value Object de violação. |
| `ReportFilePath` | `src/main/java/pt/isep/sem4pi/domain/model/simulation/ReportFilePath.java` | Value Object do caminho. |
| `SimulationValidationResult` | `src/main/java/pt/isep/sem4pi/domain/model/simulation/SimulationValidationResult.java` | Enum `PASS/FAIL`. |
| `SimulationSummaryReportExporter` | `src/main/java/pt/isep/sem4pi/application/service/simulation/SimulationSummaryReportExporter.java` | Criar/eliminar ficheiro US111. |
| `SimulationReportRepository` | `src/main/java/pt/isep/sem4pi/domain/repositories/SimulationReportRepository.java` | Contrato de persistência. |
| `JpaSimulationReportRepository` | `src/main/java/pt/isep/sem4pi/infrastructure/persistence/JPA/JpaSimulationReportRepository.java` | Implementação JPA. |
| `RepositoryFactory` | `src/main/java/pt/isep/sem4pi/infrastructure/persistence/RepositoryFactory.java` | Criar repositórios e contexto transacional. |
| `ViewSimulationReportUI` | `src/main/java/pt/isep/sem4pi/presentation/simulation/ViewSimulationReportUI.java` | Apresentar relatório. |
| `CheckSimulationStatusController` | `src/main/java/pt/isep/sem4pi/application/controller/simulation/CheckSimulationStatusController.java` | Autorizar e consultar. |
| `SimulationSummaryReportDTOMapper` | `src/main/java/pt/isep/sem4pi/application/viadto/SimulationSummaryReportDTOMapper.java` | Mapear aggregate para DTO. |
| `SimulationSummaryReportDTO` | `src/main/java/pt/isep/sem4pi/domain/model/simulation/DTO/SimulationSummaryReportDTO.java` | Transportar dados para UI. |
| `SimulationStatusDTO` | `src/main/java/pt/isep/sem4pi/domain/model/simulation/DTO/SimulationStatusDTO.java` | Transportar estado e relatório opcional. |

## 4. Classes importantes e defesa

### `GenerateSimulationSummaryReportService`

Existe para coordenar parser, criação do domínio, exportação e persistência.

Foi implementado como Application Service porque a operação atravessa vários
aggregates e recursos externos. Não pertence a `SimulationReport`, pois uma
entidade de domínio não deve abrir ficheiros, criar transações ou obter
repositórios.

Padrões/princípios: Service Layer, Transaction Script controlado, Low Coupling.

**Pergunta:** Porque o serviço não contém o parsing e a escrita?

**Resposta curta:** Para manter alta coesão e permitir testar/substituir cada
responsabilidade.

**Resposta completa:** O serviço decide a sequência. O parser conhece o formato
SCOMP, o aggregate conhece regras, o exporter conhece ficheiros e os
repositories conhecem persistência.

### `SimulationReportParser`

Existe como fronteira entre o relatório textual externo e os objetos Java.

Foi implementado separadamente para impedir que detalhes como regex, títulos de
secção e linhas SCOMP entrem no domínio. Não se persistem linhas diretamente
porque isso não garante consistência semântica.

Padrões/princípios: Anti-Corruption Layer, Adapter, Protected Variations.

**Pergunta:** Porque validar contagens?

**Resposta curta:** Para rejeitar um relatório truncado ou contraditório.

**Resposta completa:** Um resumo com total 3 e apenas 2 estados, ou com 2
violações mas uma linha, não é uma base segura para decisão operacional.

### `ParsedSimulationReport`

Existe como resultado interno, imutável e tipado do parser.

Não é o aggregate persistido porque ainda não tem identidade, caminho do
ficheiro US111 nem regra final completa. Usa cópias das listas para impedir
alterações externas.

**Pergunta:** É DTO?

**Resposta curta:** É um modelo de transferência interno entre parser e serviço.

**Resposta completa:** Tem função semelhante a DTO, mas não atravessa a
fronteira da UI; representa dados já interpretados antes da criação do domínio.

### `SimulationReport`

É o Aggregate Root da US111. Possui identidade própria, metadados, coleções de
Value Objects e regra `PASS/FAIL`.

Foi implementado assim para garantir que nunca existe um relatório cujo
resultado contradiz violações ou voos não concluídos. Não se confia cegamente no
`PASS` do SCOMP.

Padrões/princípios: Aggregate Root, Information Expert, High Cohesion.

**Pergunta:** Porque recalcular o resultado se o SCOMP já o fornece?

**Resposta curta:** Defesa em profundidade e preservação da regra do agregado.

**Resposta completa:** O resultado externo é uma entrada. O aggregate garante
que `PASS` só é possível quando SCOMP passou, não há violações e todos os voos
estão `FINISHED`.

### Value Objects

`FlightExecutionSummary`, `SafetyViolationSummary` e `ReportFilePath` são
`@Embeddable`, não têm repository próprio e são comparados pelo valor.

**Pergunta:** Porque são Value Objects?

**Resposta curta:** Não têm identidade nem ciclo de vida fora do relatório.

**Resposta completa:** Uma violação deste resumo interessa pelo conjunto dos
seus valores. É criada, armazenada e removida com o Aggregate Root.

### `SimulationSummaryReportExporter`

Existe para gerar o ficheiro legível da US111.

Escreve para `.tmp` e depois faz move. Isto reduz a janela em que um leitor
encontraria um ficheiro parcial. Não escreve diretamente no destino porque uma
falha a meio deixaria conteúdo incompleto com nome final.

**Pergunta:** O move é sempre atómico?

**Resposta curta:** É tentado `ATOMIC_MOVE`; existe fallback para sistemas que
não o suportam.

### TransactionalContext

O serviço grava `SimulationReport` e atualiza `Simulation` na mesma transação.

Foi feito assim porque os dois aggregates têm de refletir o mesmo resultado. Não
se usam duas transações independentes porque poderia existir relatório `PASS`
com simulação ainda `RUNNING`.

**Pergunta:** O ficheiro participa na transação da BD?

**Resposta curta:** Não; é usado um mecanismo de compensação.

**Resposta completa:** O sistema de ficheiros não é transacional com JPA. O
ficheiro é criado primeiro; se a BD falhar, há rollback e eliminação do ficheiro.

### Repositories e Factory

As interfaces estão orientadas a aggregates. `RepositoryFactory` cria versões
auto-transacionais ou ligadas a um `TransactionalContext`.

Não se instancia `JpaSimulationReportRepository` no serviço porque isso
acoplaria aplicação a JPA e impediria facilmente testes em memória.

### DTO e Mapper

O DTO contém apenas dados necessários à apresentação. O Mapper centraliza a
conversão das coleções internas.

Não se entrega `SimulationReport` diretamente à UI porque isso exporia o modelo
de domínio/persistência e permitiria dependências indevidas.

### `@Version` e Optimistic Locking

`SimulationReport` e `Simulation` têm `@Version`. Cada update inclui a versão
carregada. Se outra transação já atualizou o registo, o JPA deteta conflito.

Foi escolhido Optimistic Locking porque conflitos são esperados como raros. Um
bloqueio pessimista durante parsing e escrita de ficheiros seria caro e
desnecessário.

## 5. Responsabilidade por camada

| Camada | Responsabilidade |
|---|---|
| Apresentação | Pedir ID e mostrar DTO. |
| Controller | Autorizar e expor operação de consulta. |
| Aplicação | Orquestrar parsing, exportação, transação e mapping. |
| Domínio | Validar objetos e calcular `PASS/FAIL`. |
| Repository abstraction | Disponibilizar aggregates sem revelar JPA. |
| Infraestrutura | Implementar JPA e selecionar factory. |
| Externo/SCOMP | Produzir os factos técnicos originais. |

## 6. Princípios e padrões usados

- Aggregate Root: `SimulationReport`.
- Value Object: resumos e caminho.
- Repository: `SimulationReportRepository`.
- DTO: `SimulationSummaryReportDTO`.
- Mapper: `SimulationSummaryReportDTOMapper`.
- Service: `GenerateSimulationSummaryReportService`.
- Controller: `CheckSimulationStatusController`.
- Factory: `RepositoryFactory`/`PersistenceContext`.
- Anti-Corruption Layer: `SimulationReportParser`.
- Compensating Transaction: apagar ficheiro se a BD falhar.
- Protected Variations: parser/exporter/repository atrás de responsabilidades
  estáveis.
- High Cohesion: cada classe conhece uma tarefa.
- Low Coupling: domínio não conhece ficheiros, UI ou JPA.

## 7. Perguntas que quase de certeza vão aparecer

1. **Qual é o Aggregate Root?**  
   `SimulationReport`.

2. **Onde é decidida a validação final?**  
   No construtor de `SimulationReport`, através de
   `calculateValidationResult`.

3. **Porque há Value Objects?**  
   Para representar partes sem identidade própria e preservar significado.

4. **Porque existe parser?**  
   Para isolar o formato externo SCOMP.

5. **Porque há DTO e Mapper?**  
   Para não expor o aggregate à UI.

6. **O que garante o TransactionalContext?**  
   Atomicidade entre guardar relatório e atualizar simulação.

7. **Como tratam ficheiro + BD?**  
   Escrita primeiro, transação depois e eliminação compensatória em falha.

8. **Para que serve `@Version`?**  
   Optimistic Locking.

9. **Porque Repository é interface?**  
   Para independência de JPA e testabilidade.

10. **Onde é consultado o relatório?**  
    `ViewSimulationReportUI -> CheckSimulationStatusController -> Repository ->
    Mapper -> DTO`.

## 8. Perguntas difíceis

### Porque o aggregate do relatório é separado de `Simulation`?

Têm identidades, responsabilidades e ciclos de vida distintos. A simulação
controla execução; o relatório representa uma evidência gerada. A transação
coordena ambos sem os fundir num aggregate excessivamente grande.

### Porque `simulationId` é unique no relatório?

O modelo assume um relatório final por simulação. A consulta principal é
`findBySimulationId`, e a unicidade impede resumos finais concorrentes para a
mesma execução.

### Porque as coleções são `EAGER`?

O relatório é consultado como um resumo completo e as coleções fazem parte da
sua representação. A decisão reduz problemas de sessão fechada na UI, embora em
volumes muito grandes fosse uma decisão a reavaliar.

### O parser com regex não é frágil?

É sensível ao contrato textual, por isso está isolado. A vantagem arquitetural é
que uma mudança do formato fica concentrada no Anti-Corruption Layer e nos seus
testes, sem alterar aggregate, controller ou UI.

### O ficheiro persistido em `SimulationReport.fileLocation` é o ficheiro SCOMP?

Não. O parser recebe o caminho do relatório técnico SCOMP, mas o aggregate
guarda o caminho do novo resumo criado por `SimulationSummaryReportExporter`.
Esta distinção é importante para a integração com a US112.

### Porque o exporter está no package de application service?

Na implementação atual é uma colaboração do serviço e não contém regra de
domínio. Conceptualmente é uma preocupação de saída/infraestrutura; a defesa
deve reconhecer que a separação de responsabilidade existe, mesmo que a
localização física pudesse ser discutida.

### O rollback desfaz o ficheiro?

Não automaticamente. O rollback é apenas da BD. O método `deleteExportedReport`
é a compensação explícita.

## 9. Folha A4 - revisão rápida

### Frase de abertura

US111 transforma o relatório técnico SCOMP num Aggregate Root consistente,
ficheiro legível e metadados persistidos.

### Fluxo

`Job -> GenerateService -> Parser -> ParsedReport -> SimulationReport ->
Exporter -> TransactionalContext -> dois Repositories -> commit -> Controller
de consulta -> Mapper -> DTO -> UI`

### Decorar

- Aggregate Root: `SimulationReport`.
- VOs: `FlightExecutionSummary`, `SafetyViolationSummary`, `ReportFilePath`.
- Regra: SCOMP FAIL ou violações ou voo não FINISHED = FAIL.
- Parser: Anti-Corruption Layer.
- Exporter: `.tmp` + move.
- Transação: relatório + estado de simulação.
- Compensação: apagar ficheiro se persistência falhar.
- Repository: abstrai JPA.
- DTO/Mapper: protegem domínio da UI.
- `@Version`: Optimistic Locking.
- High Cohesion: parser parseia, aggregate decide, exporter escreve.
- Low Coupling: cada camada comunica por contrato.

### Resposta de 20 segundos

Depois do SCOMP, o serviço US111 usa um parser como Anti-Corruption Layer. Cria
o Aggregate Root `SimulationReport`, composto por Value Objects e responsável
pela regra final `PASS/FAIL`. O exporter escreve atomicamente o ficheiro e um
`TransactionalContext` guarda o relatório e atualiza `Simulation` na mesma
transação. Na consulta, um Mapper cria DTOs para a UI não receber entidades.
