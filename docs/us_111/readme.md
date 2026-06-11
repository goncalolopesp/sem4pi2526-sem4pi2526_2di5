# US111 - Generate a Simulation Report

## 1. Requirements

As a Flight Control Operator, I want to receive a summary of the
simulation results so that I can determine if the programmed flights are
safe to run.

Acceptance criteria:

- generate the summary report and store it in a file;
- include the total number of flights;
- include each flight execution status;
- list the timestamp and position of each safety violation;
- indicate whether the scheduled flights plan passed or failed validation.

US111 depends on the simulation execution and data produced by US100,
US102, US109 and US110. The Java application does not reproduce the SCOMP
simulation or reporting algorithms. It reads the final US109 report after
the asynchronous SCOMP execution completes.

## 2. Analysis

The SCOMP report is the source of the simulation facts. It already contains
the total flights, final execution statuses, position history, safety
violations and final validation result.

The EAPLI side parses those facts into:

- `FlightExecutionSummary`;
- `SafetyViolationSummary`;
- `SimulationValidationResult`.

`SimulationReport` is the aggregate root for the persisted US111 metadata.
It owns the validation rule: the result is `PASS` only when SCOMP completed
successfully, all flight executions finished, and no safety violations were
reported. Otherwise, it is `FAIL`.

## 3. Design

`SimulationJobService` preserves the existing asynchronous flow:

1. load and mark the simulation as running;
2. execute `scomp/flight_simulator`;
3. parse the US109 report;
4. build and export the US111 summary;
5. persist the report and update the simulation status.

`GenerateSimulationSummaryReportService` coordinates the report use case.
The UI accesses report information only through
`CheckSimulationStatusController`. `SimulationSummaryReportDTO` prevents
JPA/domain objects from leaking into the presentation layer.

Flight Control Operators and administrators can use
`Simulation > View Simulation Report` to consult the complete summary by
simulation id. If the asynchronous execution has not finished yet, the UI
informs the operator that the report is not available.

The human-readable files are stored under:

```text
reports/simulation/<simulation-id>-<yyyyMMdd-HHmmss-SSS>.txt
```

The simulation id and generation timestamp prevent different executions
from overwriting one another.

### Transaction

The file is first written atomically through a temporary file. A
`TransactionalContext` is then started and transaction-bound repository
instances save both the `SimulationReport` and final `Simulation` status.
The transaction commits only after both saves succeed.

On persistence failure, the transaction is rolled back and the exported
file is deleted as a compensating action. An export failure occurs before
the transaction and therefore leaves no report metadata in the database.

## 4. Tests

Automated tests cover:

- no violations produces `PASS`;
- violations produce `FAIL`;
- timestamps and positions are exported;
- total flights and execution statuses are exported;
- the summary file is created;
- metadata is available through the report repository;
- a persistence failure rolls back and removes the exported file;
- a failure in the second aggregate save rolls back the report save;
- an export failure does not start a database transaction;
- the real JPA mapping persists and reloads report metadata and collections;
- the parser accepts SCOMP separation violations and rejects incomplete
  violation listings;
- aborted or otherwise unfinished flights force `FAIL`.

Run the focused tests with:

```sh
mvn "-Dtest=GenerateSimulationSummaryReportServiceTest,SimulationReportParserTest,SimulationReportTest,JpaSimulationReportMappingTest" test
```

## 5. AntiX Execution

Compile SCOMP before starting the Java application:

```sh
cd scomp
make
cd ..
sed -i 's/\r$//' run.sh
chmod +x run.sh
chmod +x scomp/flight_simulator
./run.sh
```

The `sed` command is only needed when `run.sh` has Windows line endings.
This flow keeps the existing EAPLI -> SCOMP -> EAPLI file integration and
does not introduce an RCOMP dependency.
