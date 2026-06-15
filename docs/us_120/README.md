# US120 — Flight Plan Validation (ANTLR4)

## 1. Requirements Engineering

### 1.1 User Story

As an **Air Transport Company Collaborator**, I want to validate flight plan files against the Core Flight DSL so that only structurally and semantically correct plans can be imported or tested.

### 1.2 Acceptance Criteria

- Input files must use the `.dsldig5` extension.
- Lexical and syntactic errors are detected by the ANTLR parser.
- Semantic rules are enforced after a successful parse (listener + visitor).
- If lexical/syntactic validation fails, semantic validation is skipped.
- Validation results are reported with clear error messages.

### 1.3 Dependencies

- **ANTLR4:** grammar compilation during `mvn compile`.
- **US080 / US081 / US085:** downstream consumers of validated parse trees.

---

## 2. Grammar Structure

The grammar follows the `FlightPlan` aggregate. A flight plan declares, at the top level, the extracted flight id, flight type, assigned aircraft, pilot, and route, followed by one or more legs:

```text
flight <ID> <regular|charter> {
    aircraft <REGISTRATION>;
    pilot <PILOT_ID>;
    route <ID> { (lat|lon) ... };
    leg {
        departure { <AIRPORT>, <DATE>, <TIME> };
        arrival { <AIRPORT>, <TIME> };
        segment { (lat|lon), (lat|lon), {(min,max)}, <width>, <windAngle>, <speed> };
        fuel { <quantity>, <unit> };
    };
};
```

Grammar source: `src/main/antlr4/US120.g4`

---

## 3. Validation Architecture

Validation is split into two strict phases. If the first phase fails, the second phase is automatically skipped to prevent processing corrupted parse trees.

```text
US120FlightPlanValidator.analyze(path)
        │
        ├── Phase 1: Lexical + Syntactic (US120ErrorListener)
        │       └── lexer + parser on .dsldig5 file
        │
        └── Phase 2: Semantic (only if Phase 1 passes)
                ├── US120SemanticListener  (leg/segment/route coherence)
                └── US120SemanticVisitor   (field-level business rules)
```

| Component | Responsibility |
|-----------|----------------|
| `US120Lexer` / `US120Parser` | Generated from `US120.g4` |
| `US120ErrorListener` | Unrecognized characters, missing braces, misspelled keywords |
| `US120SemanticListener` | Consecutive legs, temporal order, coordinate continuity, route coverage |
| `US120SemanticVisitor` | Dates, times, fuel, altitudes, speeds, coordinates |
| `US120ValidationResult` | Groups lexical/syntactic and semantic errors; holds parse tree on success |

### 3.1 Semantic Rules (Visitor)

| Rule | Constraint |
|------|------------|
| File extension | Must be `.dsldig5` |
| Aircraft registration | Must not be empty |
| Pilot identifier | Must not be empty |
| Dates | Valid calendar date via `LocalDate` |
| Times | Hours 0–23, minutes/seconds 0–59 |
| Latitude | −90 to 90 |
| Longitude | −180 to 180 |
| Altitude slots | Minimum ≤ maximum |
| Wind angle | [0, 360[ |
| Width, speed | Must be > 0 |

---

## 4. Integration Points

| Consumer | Usage |
|----------|-------|
| `ValidateFlightPlanUI` | Standalone validation menu (**Air Transport Company > Validate flight plan**) |
| `ImportFlightPlanService` (US081) | Validates before import |
| `CreateFlightPlanFromFileService` (US121) | Validates after format detection |
| `TestFlightPlanService` (US085) | Re-validates stored DSL before C test |
| `US081FlightPlanMapper` | Maps validated parse tree to `CreateFlightPlanDTO` |

---

## 5. How to Run

Compile and start the application as usual (`run.sh`, `run-client.sh`, or `mvn exec:java`). After login, open **Air Transport Company > Validate flight plan** and enter the path to a `.dsldig5` file (e.g. `docs/us_120/testFiles/exemplo.dsldig5`).

The ANTLR lexer, parser, visitor, and listener are generated automatically during `mvn compile` from `src/main/antlr4/US120.g4`.

---

## 6. Example Files

| File | Purpose |
|------|---------|
| `docs/us_120/testFiles/exemplo.dsldig5` | Reference valid file |
| `docs/us_081/examples/exampleValid.dsldig5` | Valid import example |
| `docs/us_081/examples/exampleInvalid.dsldig5` | Invalid file for error testing |

---

## 7. Tests

- `US120SemanticValidationTest`
- Covered indirectly by `ImportFlightPlanServiceTest` and `TestFlightPlanServiceTest`

Run:

```bash
mvn test
```

---

## 8. Related Stories

- **US081:** import validated DSL into the domain model.
- **US121:** format detection + US120 validation.
- **US085:** re-validation before C flight test.
- **US080:** domain aggregate created after successful validation and mapping.
