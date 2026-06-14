# US121 — Create a Flight Plan From a File

## User Story

As a **Pilot**, I want to create a valid flight plan from a file. There may be multiple file formats; the system must validate the format before using the file.

## Acceptance Criteria

| Criterion | Implementation |
|-----------|----------------|
| File conforms to the Flight DSL | `.dsldig5` format (Core Flight DSL / US120) |
| Validation according to US120 | `US120FlightPlanValidator` (lexical, syntactic, and semantic) |
| Invalid files with clear messages | Errors grouped and displayed in the UI |
| Only valid plans are imported | Import blocked if format or US120 validation fails |

**Note:** The validation dependency is **US120** (Flight DSL). US112 in this project covers monthly reports and is not related to this user story.

## Architecture

```text
CreateFlightPlanFromFileUI (US121)
        │
        ▼
CreateFlightPlanFromFileController
        │
        ▼
CreateFlightPlanFromFileService
        ├── FlightPlanFileFormat  (format detection)
        ├── US120FlightPlanValidator
        └── ImportFlightPlanService → CreateFlightPlanService → domain
```

## Supported File Formats

| Extension | Description |
|-----------|-------------|
| `.dsldig5` | Core Flight DSL (US120) |

Other formats (e.g. `.json`, `.txt`) are rejected with a message listing the supported formats.

## Flow

1. Pilot opens **Flight Planning > Create flight plan from file (US121)**.
2. Enters the path to a `.dsldig5` file.
3. System validates file format and US120 rules.
4. If valid, the flight plan is created in status **DRAFT** (US080 domain rules).
5. If invalid, errors are listed and **nothing** is persisted.

## Example Files (bootstrap data)

These examples use entities created at startup:

| Entity | Bootstrap value |
|--------|-----------------|
| Flight | `TP100` |
| Route | `TP1001` (OPO → MAD) |
| Aircraft | `CS-TST` |
| Pilot | `PILOT01` (company TP) |

- Valid: `docs/us_121/examples/exampleValid.dsldig5`
- Invalid: `docs/us_121/examples/exampleInvalid.dsldig5`
- Reference from US120: `docs/us_120/testFiles/exemplo.dsldig5`

## Manual Test

1. Login as `pilot` / `Password1`.
2. **Flight Planning > Create flight plan from file (US121)**.
3. Try `docs/us_121/examples/exampleInvalid.dsldig5` — expect semantic/syntactic errors.
4. Try a valid `.dsldig5` with route `TP1001`, aircraft `CS-TST`, pilot `PILOT01` — expect success.

## Tests

- `CreateFlightPlanFromFileServiceTest`
- `CreateFlightPlanFromFileControllerTest`
- Existing `ImportFlightPlanServiceTest` (import pipeline)

Run:

```bash
mvn test
```

## Related Stories

- **US080** — flight plan domain model and creation rules
- **US120** — Flight DSL specification and validation
- **US081** — earlier import implementation (reused internally)
- **US085** — test/validate imported plan
