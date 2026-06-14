# US111 - Gerar um Relatório de Simulação

## 1. Requisitos

Enquanto Operador de Controlo de Voo, pretendo receber um resumo dos resultados
da simulação, para determinar se os voos programados podem ser realizados em
segurança.

Critérios de aceitação:

- gerar o relatório de resumo e guardá-lo num ficheiro;
- incluir o número total de voos;
- incluir o estado de execução de cada voo;
- listar o instante e a posição de cada violação de segurança;
- indicar se o plano de voos programado passou ou falhou a validação.

A US111 depende da execução da simulação e dos dados produzidos pelas US100,
US102, US109 e US110. A aplicação Java não repete os algoritmos de simulação ou
de geração de relatórios do SCOMP. Após a conclusão assíncrona da execução,
limita-se a ler o relatório final da US109.

## 2. Análise

O relatório do SCOMP é a fonte dos factos da simulação. Contém o total de voos,
os estados finais de execução, o histórico de posições, as violações de
segurança e o resultado final da validação.

No lado EAPLI, estes dados são convertidos em:

- `FlightExecutionSummary`;
- `SafetyViolationSummary`;
- `SimulationValidationResult`.

`SimulationReport` é a raiz do agregado responsável pelos metadados persistidos
da US111. O resultado só é `PASS` quando o SCOMP termina com sucesso, todos os
voos terminam a execução e não são comunicadas violações de segurança. Em
qualquer outro caso, o resultado é `FAIL`.

## 3. Desenho

### 3.1 Diagramas

- [SSD](ssd.puml)

O `SimulationJobService` preserva o fluxo assíncrono existente:

1. carrega a simulação e marca-a como em execução;
2. executa `scomp/flight_simulator`;
3. interpreta o relatório da US109;
4. constrói e exporta o resumo da US111;
5. persiste o relatório e atualiza o estado da simulação.

O `GenerateSimulationSummaryReportService` coordena o caso de uso. A interface
acede aos dados apenas através de `CheckSimulationStatusController`.
`SimulationSummaryReportDTO` impede a exposição de objetos JPA ou de domínio à
camada de apresentação.

Os Operadores de Controlo de Voo e administradores podem usar
`Simulation > View Simulation Report` para consultar o resumo completo através
do identificador da simulação. Se a execução assíncrona ainda não tiver
terminado, a interface informa que o relatório ainda não está disponível.

Os ficheiros legíveis são guardados em:

```text
reports/simulation/<simulation-id>-<yyyyMMdd-HHmmss-SSS>.txt
```

O identificador e o instante de geração impedem que execuções diferentes
substituam os ficheiros umas das outras.

### Transação

O ficheiro é primeiro escrito atomicamente através de um ficheiro temporário.
Em seguida, é iniciado um `TransactionalContext`. Os repositórios associados à
transação guardam o `SimulationReport` e o estado final de `Simulation`. A
transação só é confirmada quando ambas as operações terminam com sucesso.

Se a persistência falhar, a transação é revertida e o ficheiro exportado é
eliminado como ação de compensação. Uma falha de exportação acontece antes da
transação e, por isso, não deixa metadados de relatório na base de dados.

## 4. Testes

Os testes automatizados verificam:

- ausência de violações produz `PASS`;
- existência de violações produz `FAIL`;
- exportação dos instantes e posições;
- exportação do total de voos e dos estados de execução;
- criação do ficheiro de resumo;
- consulta dos metadados através do repositório;
- reversão e remoção do ficheiro quando a persistência falha;
- reversão da gravação do relatório quando falha o segundo agregado;
- ausência de transação quando a exportação falha;
- persistência e recarregamento dos metadados e coleções através de JPA;
- aceitação das violações de separação do SCOMP e rejeição de listagens
  incompletas;
- resultado `FAIL` para voos abortados ou não concluídos.

Execução dos testes específicos:

```sh
mvn "-Dtest=GenerateSimulationSummaryReportServiceTest,SimulationReportParserTest,SimulationReportTest,JpaSimulationReportMappingTest" test
```

## 5. Execução em AntiX

O componente SCOMP deve ser compilado antes do arranque da aplicação Java:

```sh
cd scomp
make
cd ..
sed -i 's/\r$//' run.sh
chmod +x run.sh
chmod +x scomp/flight_simulator
./run.sh
```

O comando `sed` só é necessário quando `run.sh` contém terminações de linha do
Windows. Este fluxo mantém a integração por ficheiros
EAPLI -> SCOMP -> EAPLI e não introduz uma dependência de RCOMP.
