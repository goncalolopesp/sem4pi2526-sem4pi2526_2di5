# US112 - Gerar Relatório Mensal

## 1. Objetivo

A US112 gera um relatório estatístico mensal a partir dos dados já armazenados
pelo AIControl. O relatório é exportado para um ficheiro de texto e os seus
metadados são persistidos para consulta posterior.

A aplicação de consola não possui uma biblioteca gráfica de relatórios. O
renderizador atual utiliza secções de texto identificadas com a marca do
projeto e resumos alinhados. O contrato de renderização permite adicionar
futuramente formatos gráficos, HTML, PDF ou com gráficos, sem alterar a recolha
dos dados mensais.

## 2. Arquitetura

A implementação segue as camadas EAPLI:

1. **Apresentação** - `GenerateMonthlyReportUI` lê o mês e apresenta um
   `GeneratedReportDTO`.
2. **Controlador** - `GenerateMonthlyReportController` verifica a autorização
   e invoca o serviço de aplicação.
3. **Aplicação** - `GenerateMonthlyReportService` coordena a recolha, construção
   e renderização do relatório, o armazenamento do ficheiro e a persistência
   dos metadados.
4. **Domínio** - `Report`, `ReportSection` e `ReportMetadata` definem a
   estrutura do relatório e o agregado de metadados persistidos.
5. **Repositórios** - `ReportMetadataRepository` possui implementações JPA e em
   memória, obtidas através de `RepositoryFactory`.
6. **Infraestrutura** - `MonthlyReportServiceFactory` compõe as dependências
   concretas e `SimulationReportFileViolationReader` lê os ficheiros SCOMP.

O controlador autoriza exclusivamente o papel
`FLIGHT_CONTROL_OPERATOR`, conforme definido no enunciado.

Os metadados são guardados dentro de um `TransactionalContext` explícito. Se a
persistência falhar, o ficheiro gerado é eliminado, evitando que o sistema de
ficheiros e a base de dados apresentem resultados contraditórios.

## 3. Separação de Responsabilidades

### Recolha de Dados

`ReportDataCollector<T>` é independente da apresentação e da exportação.
`MonthlyStatisticsDataCollector` lê:

- simulações cujo `startTime` pertence ao mês selecionado;
- relatórios de simulação cujo `generatedAt` pertence ao mês selecionado;
- totais de violações de segurança presentes nos ficheiros compatíveis com a
  US111;
- voos cuja `departureDate` pertence ao mês selecionado.

Não são estimados nem inventados valores. Se um relatório antigo ou
indisponível não contiver um total de violações legível, contribui com zero
violações.

A leitura física do ficheiro não pertence ao coletor. É delegada através de
`SafetyViolationTotalReader` à implementação de infraestrutura
`SimulationReportFileViolationReader`.

### Estrutura do Relatório

`MonthlyStatisticsReportBuilder` converte `MonthlyStatisticsData` num
`MonthlyStatisticsReport` composto por objetos `ReportSection` claramente
separados. Não acede a repositórios nem escreve ficheiros.

### Renderização

`ReportRenderer` exporta qualquer `Report`. `TextReportRenderer` fornece o
formato atual, compatível com a consola, incluindo cabeçalho AIControl, título,
período, instante de geração e organização consistente das secções.

O relatório inclui ainda a secção `Monthly Overview Chart`, com barras
textuais. Esta representação concretiza os gráficos pedidos sem acoplar a
aplicação de consola a uma biblioteca gráfica.

## 4. Diagramas

- [SSD](ssd.puml)

## 5. Extensão da Estrutura

Para adicionar outro tipo de relatório, por exemplo de conformidade ou de
incidentes:

1. criar o objeto de dados e uma implementação de `ReportDataCollector`;
2. criar um subtipo ou construtor de `Report` que defina as secções;
3. reutilizar um renderizador existente;
4. adicionar apenas a composição do respetivo controlador e serviço.

Para adicionar um formato, basta implementar `ReportRenderer` e injetá-lo no
serviço de aplicação. A recolha mensal e a construção das secções permanecem
inalteradas.

## 6. Saída e Compatibilidade

Os ficheiros são colocados em `reports/` e usam nomes com marca temporal. O
interpretador dos relatórios de simulação e os ficheiros da US111 não são
alterados. A US112 apenas lê `Total flights`, os metadados de geração, o
resultado da validação e o total de violações da US102 já disponibilizado pela
US111.

## 7. Testes

Os testes automatizados cobrem:

- geração de relatório com dados;
- geração de relatório sem dados;
- criação e conteúdo do ficheiro de texto;
- persistência transacional dos metadados;
- extração das violações dos relatórios de simulação existentes;
- extensibilidade através de uma segunda implementação de renderizador.
