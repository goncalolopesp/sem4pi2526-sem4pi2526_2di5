# Defesa Oral - US112

## 1. Objetivo da US

Gerar um relatório estatístico mensal a partir de dados já armazenados:

- simulações do mês e distribuição por estado;
- quantidade de inputs de voo simulados;
- relatórios de simulação e voos representados;
- violações de segurança;
- voos agendados e distribuição por estado;
- gráfico textual;
- ficheiro gerado e metadados persistidos.

## 2. Fluxo completo passo a passo

1. `GenerateMonthlyReportUI` pede ano e mês.
2. Chama `GenerateMonthlyReportController.generate`.
3. O controller verifica autorização.
4. Converte os inteiros em `YearMonth`, validando o período.
5. Chama `GenerateMonthlyReportService`.
6. O serviço obtém o instante através de `Clock`.
7. `MonthlyStatisticsDataCollector.collect` consulta repositories.
8. Filtra simulações por `Simulation.startTime`.
9. Conta simulações por `SimulationStatus` e inputs usados.
10. Filtra `SimulationReport` por `generatedAt`.
11. Soma voos reportados.
12. Para cada relatório, chama `SafetyViolationTotalReader`.
13. `SimulationReportFileViolationReader` lê o ficheiro indicado pelo relatório
    e tenta extrair o total através do padrão técnico do SCOMP.
14. O coletor filtra `Flight` por `departureDate` e conta estados.
15. Devolve `MonthlyStatisticsData`, com mapas imutáveis.
16. `MonthlyStatisticsReportBuilder` converte dados em `ReportSection`.
17. Cria `MonthlyStatisticsReport`, subtipo de `Report`.
18. O serviço cria ID e nome de ficheiro.
19. Chama a estratégia `ReportRenderer`.
20. `TextReportRenderer` escreve o formato atual.
21. O serviço cria o Aggregate Root `ReportMetadata`.
22. Abre `TransactionalContext`.
23. Obtém `ReportMetadataRepository` associado à transação.
24. Guarda metadados e faz `commit`.
25. Se a persistência falhar, faz `rollback` e apaga o ficheiro.
26. Cria `GeneratedReportDTO`.
27. A UI apresenta ID e localização.

## 3. Classes e caminhos por ordem

| Elemento | Caminho | Papel |
|---|---|---|
| `GenerateMonthlyReportUI` | `src/main/java/pt/isep/sem4pi/presentation/reporting/GenerateMonthlyReportUI.java` | Ler período e mostrar resultado. |
| `GenerateMonthlyReportController` | `src/main/java/pt/isep/sem4pi/application/controller/reporting/GenerateMonthlyReportController.java` | Autorizar e converter para `YearMonth`. |
| `MonthlyReportServiceFactory` | `src/main/java/pt/isep/sem4pi/infrastructure/reporting/MonthlyReportServiceFactory.java` | Compor dependências concretas. |
| `GenerateMonthlyReportService` | `src/main/java/pt/isep/sem4pi/application/service/reporting/GenerateMonthlyReportService.java` | Orquestrar o caso de uso. |
| `ReportDataCollector` | `src/main/java/pt/isep/sem4pi/application/service/reporting/ReportDataCollector.java` | Contrato genérico de recolha. |
| `MonthlyStatisticsDataCollector` | `src/main/java/pt/isep/sem4pi/application/service/reporting/MonthlyStatisticsDataCollector.java` | Agregar os dados do mês. |
| `MonthlyStatisticsData` | `src/main/java/pt/isep/sem4pi/application/service/reporting/MonthlyStatisticsData.java` | Snapshot imutável das estatísticas. |
| `SafetyViolationTotalReader` | `src/main/java/pt/isep/sem4pi/application/service/reporting/SafetyViolationTotalReader.java` | Contrato para obter violações. |
| `SimulationReportFileViolationReader` | `src/main/java/pt/isep/sem4pi/infrastructure/reporting/SimulationReportFileViolationReader.java` | Ler o formato físico do relatório. |
| `MonthlyStatisticsReportBuilder` | `src/main/java/pt/isep/sem4pi/application/service/reporting/MonthlyStatisticsReportBuilder.java` | Construir secções e gráfico. |
| `MonthlyStatisticsReport` | `src/main/java/pt/isep/sem4pi/application/service/reporting/MonthlyStatisticsReport.java` | Especializar o tipo mensal. |
| `Report` | `src/main/java/pt/isep/sem4pi/domain/model/reporting/Report.java` | Estrutura comum de relatórios. |
| `ReportSection` | `src/main/java/pt/isep/sem4pi/domain/model/reporting/ReportSection.java` | Value Object de secção e entradas. |
| `ReportRenderer` | `src/main/java/pt/isep/sem4pi/application/service/reporting/ReportRenderer.java` | Strategy de formato. |
| `TextReportRenderer` | `src/main/java/pt/isep/sem4pi/application/service/reporting/TextReportRenderer.java` | Renderização textual. |
| `ReportMetadata` | `src/main/java/pt/isep/sem4pi/domain/model/reporting/ReportMetadata.java` | Aggregate Root persistente. |
| `ReportMetadataRepository` | `src/main/java/pt/isep/sem4pi/domain/repositories/ReportMetadataRepository.java` | Contrato de persistência. |
| `JpaReportMetadataRepository` | `src/main/java/pt/isep/sem4pi/infrastructure/persistence/JPA/JpaReportMetadataRepository.java` | Implementação JPA. |
| `GeneratedReportDTO` | `src/main/java/pt/isep/sem4pi/application/viadto/GeneratedReportDTO.java` | Resultado para a UI. |

## 4. Classes importantes e defesa

### `GenerateMonthlyReportController`

Existe para representar a operação do ator, autorizar e transformar input.

Não recolhe estatísticas nem escreve ficheiros porque essas responsabilidades
pertencem ao serviço e aos colaboradores especializados.

**Pergunta:** Porque construir `YearMonth` no controller?

**Resposta curta:** É a fronteira que transforma input primitivo num conceito
temporal válido.

**Resposta completa:** `YearMonth.of` valida mês e ano antes de entrar no caso de
uso. O serviço recebe um período sem dia artificial.

### `MonthlyReportServiceFactory`

Existe para concentrar a composição de objetos concretos.

Foi implementada fora do domínio para que controller e service não precisem de
conhecer todas as classes de infraestrutura. Não foi usado `new` espalhado pelo
serviço porque isso impediria substituição em testes.

Padrões: Factory, Dependency Injection manual, Pure Fabrication.

**Pergunta:** É Factory Method ou Abstract Factory?

**Resposta curta:** É uma factory de composição simples.

**Resposta completa:** O método estático `create` monta o grafo do caso de uso.
`RepositoryFactory`, por sua vez, é a abstração de fábrica dos repositories.

### `GenerateMonthlyReportService`

Existe para definir a sequência do caso de uso: recolher, construir, renderizar,
persistir e devolver DTO.

Não contém queries, formatação textual ou regex. Isso mantém High Cohesion nos
colaboradores e Low Coupling no orquestrador.

**Pergunta:** Porque é Service e não método de `ReportMetadata`?

**Resposta curta:** Porque coordena múltiplos recursos e objetos.

**Resposta completa:** Um Aggregate Root não deve obter repositories, ler três
tipos de dados, escolher renderer, escrever ficheiros e gerir transações.

### `ReportDataCollector` e `MonthlyStatisticsDataCollector`

A interface protege o caso de uso da forma concreta de recolha. O coletor mensal
filtra e agrega factos, mas não decide apresentação.

Não constrói `ReportSection` porque recolha e apresentação mudam por razões
diferentes.

Padrões/princípios: Strategy, Protected Variations, High Cohesion.

**Pergunta:** Porque não fazer queries mensais específicas no repository?

**Resposta curta:** A implementação atual reutiliza os contratos existentes e
centraliza a seleção no coletor.

**Resposta completa:** Para o volume atual, `findAll` seguido de filtro mantém a
US isolada. Num sistema maior, queries por período seriam uma otimização do
repository sem mudar o contrato conceptual do coletor.

### `MonthlyStatisticsData`

Existe como snapshot entre recolha e construção.

Faz cópias imutáveis dos mapas para que o builder não observe alterações depois
da recolha. Não é Entity nem Aggregate Root porque não tem identidade nem é
persistido.

**Pergunta:** É Value Object?

**Resposta curta:** É um objeto de dados imutável por valor dentro da aplicação.

### `SafetyViolationTotalReader`

Existe para impedir que a camada de aplicação conheça `Files`, `Path` e regex.

`SimulationReportFileViolationReader` é a implementação de infraestrutura que
conhece o formato legado.

Padrões: Adapter, Dependency Inversion, Protected Variations.

**Pergunta:** Porque não usar diretamente `report.safetyViolations().size()`?

**Resposta curta:** A US mensal pretende o total publicado no artefacto
compatível e isola essa leitura atrás de um contrato.

**Resposta completa:** O reader preserva compatibilidade com o formato existente
e concentra a dependência física. Se a fonte mudar, troca-se o adaptador.

**Ponto crítico da implementação atual:** `SimulationReport.fileLocation()` é
preenchido pela US111 com o caminho do resumo US111. Esse resumo escreve
`Safety violations: N`, enquanto o reader procura
`Safety Violations (X shown / Y total)`, que é o cabeçalho do relatório técnico
SCOMP. Portanto, no fluxo integrado atual, o reader pode devolver zero mesmo
quando o aggregate US111 contém violações. Isto deve ser assumido na defesa como
uma incompatibilidade de contrato existente; não foi corrigida porque esta
tarefa não permite alterar lógica.

### `MonthlyStatisticsReportBuilder`

Existe para transformar números em estrutura sem conhecer ficheiros.

Foi usado um builder separado para manter regras de organização e gráfico fora
do coletor e renderer. Não escreve texto final porque o mesmo relatório pode ter
outro formato.

Padrões: Builder, High Cohesion.

### `Report`, `MonthlyStatisticsReport` e `ReportSection`

`Report` define a estrutura comum. `MonthlyStatisticsReport` fixa tipo e título.
`ReportSection` e `Entry` são Value Objects que representam conteúdo.

Não se colocou tudo numa `String` porque uma estrutura tipada pode ser
renderizada de diferentes formas.

**Pergunta:** `Report` é Aggregate Root?

**Resposta curta:** Não nesta implementação; é um modelo em memória não
persistido.

**Resposta completa:** O Aggregate Root persistido é `ReportMetadata`. O
conteúdo é um objeto transitório usado pelo renderer.

### `ReportRenderer` e `TextReportRenderer`

`ReportRenderer` é uma Strategy. O serviço conhece formato, extensão e operação
de renderização, mas não o algoritmo textual.

Não se usa `if (format == ...)` no serviço porque cada novo formato obrigaria a
alterar o orquestrador, violando Open/Closed e Protected Variations.

**Pergunta:** Como provar extensibilidade?

**Resposta curta:** Os testes injetam outro `ReportRenderer` sem alterar a
lógica mensal.

### `ReportMetadata`

É o Aggregate Root persistido que regista identidade, tipo, período, instante,
formato e localização.

Foi separado do conteúdo para não colocar o ficheiro inteiro na BD e para
permitir formatos diferentes. Não é apenas DTO porque tem identidade,
invariantes e persistência.

**Pergunta:** Porque guardar ano e mês em colunas separadas?

**Resposta curta:** Para persistir `YearMonth` de forma simples e reconstruí-lo
sem inventar um dia.

### Repository, TransactionalContext e compensação

`ReportMetadataRepository` abstrai JPA. O serviço cria um contexto, inicia,
guarda e confirma.

O ficheiro não participa na transação JPA. Se a persistência falhar, o código faz
rollback e `Files.deleteIfExists`.

**Pergunta:** Isto é atomicidade total?

**Resposta curta:** É consistência por transação de BD mais compensação no
sistema de ficheiros.

### DTO

`GeneratedReportDTO` devolve apenas o necessário para apresentação.

Não devolve `ReportMetadata` para evitar expor o Aggregate Root e o seu modelo de
persistência à UI.

### `@Version` e Optimistic Locking

`ReportMetadata.version` permite ao JPA detetar alterações concorrentes. É
otimista porque não mantém locks enquanto o relatório é preparado.

## 5. Camadas EAPLI

| Camada | Responsabilidade |
|---|---|
| Apresentação | Ler ano/mês e mostrar DTO. |
| Controller | Autorizar e validar `YearMonth`. |
| Aplicação | Coordenar e agregar dados. |
| Domínio | Estrutura de relatório, secções e metadados consistentes. |
| Repository | Abstrair coleção de aggregates. |
| Infraestrutura | JPA, leitura de ficheiros e composição concreta. |

## 6. Conceitos pedidos

- **Aggregate Root:** `ReportMetadata`.
- **Value Object:** `ReportSection`, `Entry` e snapshots imutáveis.
- **Repository:** `ReportMetadataRepository`.
- **DTO:** `GeneratedReportDTO`.
- **Mapper:** nesta US a conversão final é simples e feita no service; não existe
  classe Mapper dedicada.
- **Service:** `GenerateMonthlyReportService`.
- **Controller:** `GenerateMonthlyReportController`.
- **Factory:** `MonthlyReportServiceFactory` e `RepositoryFactory`.
- **TransactionalContext:** transação explícita dos metadados.
- **@Version:** campo técnico de versão.
- **Optimistic Locking:** conflito detetado no update, sem lock prolongado.
- **Protected Variations:** collector, reader e renderer são contratos.
- **High Cohesion:** cada classe tem uma razão principal para mudar.
- **Low Coupling:** formato, recolha e persistência são substituíveis.

## 7. Perguntas que quase de certeza vão aparecer

1. **Qual é o Aggregate Root?**  
   `ReportMetadata`.

2. **Porque `Report` não é o Aggregate Root?**  
   É transitório e não é persistido; os metadados têm identidade e ciclo de
   vida.

3. **Qual o padrão do renderer?**  
   Strategy.

4. **Qual o padrão do builder?**  
   Builder.

5. **Para que serve a factory?**  
   Para montar dependências concretas fora do caso de uso.

6. **Como é feita a transação?**  
   `beginTransaction`, `save`, `commit`; em erro, `rollback`.

7. **O ficheiro está dentro da transação?**  
   Não; é eliminado por compensação.

8. **Porque há DTO?**  
   Para não expor o Aggregate Root à UI.

9. **Como se adiciona PDF?**  
   Implementar `ReportRenderer` e injetá-lo.

10. **Onde estão High Cohesion e Low Coupling?**  
    Collector recolhe, builder estrutura, renderer apresenta, repository
    persiste.

## 8. Perguntas difíceis

### Porque o reader devolve zero quando não consegue ler?

É uma decisão de robustez do relatório mensal: dados antigos ou ficheiros
indisponíveis não impedem a geração. A consequência é que zero significa
"nenhum total legível", não necessariamente prova absoluta de ausência.

### O total mensal de violações está garantido no fluxo integrado?

Não completamente na implementação atual. Os testes do reader usam um ficheiro
com o cabeçalho SCOMP esperado, mas o exporter US111 gera outro texto. A
arquitetura de abstração está correta, mas o contrato concreto entre exporter e
reader não está alinhado.

### O gráfico textual satisfaz um gráfico?

Na aplicação de consola, representa magnitude com barras e mantém o renderer
sem dependência gráfica. A abstração permite introduzir HTML/PDF/gráficos reais
sem alterar recolha.

### Porque o renderer escreve diretamente e não usa ficheiro temporário?

É a implementação atual da US112. A consistência com a BD é tratada por
compensação. A escrita atómica seria uma melhoria técnica possível, mas não é
necessária para explicar o desenho atual e não foi introduzida nesta tarefa.

### Há Mapper na US112?

Não há classe Mapper dedicada. Como o DTO tem poucos campos e a conversão é
direta, o service constrói-o. Um Mapper só se justificaria se removesse
complexidade real ou seguisse um padrão uniforme exigido pelo projeto.

### Porque `findAll` pode ser problemático?

Escala mal para muitos dados. É aceitável no âmbito atual, mas o repository
poderia ganhar consultas por período sem alterar collector, builder ou
renderer, demonstrando Protected Variations.

### Porque metadados e ficheiro estão separados?

BD é adequada a pesquisa e identidade; ficheiro é adequado ao artefacto
renderizado. A localização persistida liga os dois sem armazenar conteúdo
potencialmente grande.

## 9. Folha A4 - revisão rápida

### Frase de abertura

US112 separa recolha, estrutura, renderização e persistência para permitir novos
relatórios e formatos com baixo acoplamento.

### Fluxo

`UI -> Controller -> Factory -> GenerateService -> Collector -> Data -> Builder
-> Report/Sections -> Renderer -> ficheiro -> ReportMetadata ->
TransactionalContext -> Repository -> DTO -> UI`

### Decorar

- Aggregate Root: `ReportMetadata`.
- Value Object: `ReportSection`/`Entry`.
- DTO: `GeneratedReportDTO`.
- Service: orquestra.
- Controller: autoriza e cria `YearMonth`.
- Factory: monta dependências.
- Collector: agrega dados.
- Builder: cria secções.
- Renderer: Strategy.
- Reader: Adapter do ficheiro US111.
- Atenção: regex do reader espera cabeçalho SCOMP, não o texto atual do resumo
  US111; pode contar zero.
- Repository: abstrai JPA.
- Transação: metadados.
- Compensação: apagar ficheiro em erro.
- `@Version`: Optimistic Locking.
- Protected Variations: interfaces de collector, reader e renderer.
- High Cohesion + Low Coupling: responsabilidades separadas.

### Resposta de 20 segundos

O controller recebe o mês e chama um Application Service composto por factory.
O collector consulta repositories e cria um snapshot imutável. O builder
transforma os dados em `ReportSection`; o renderer Strategy gera o ficheiro. O
Aggregate Root `ReportMetadata` é persistido num `TransactionalContext`; se a BD
falhar, o ficheiro é apagado. O resultado chega à UI por DTO.
