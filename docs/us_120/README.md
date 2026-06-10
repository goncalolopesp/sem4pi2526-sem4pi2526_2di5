# US120 - Flight Plan Validation (ANTLR4)

This project implements a syntactic and semantic analyzer for flight plan files using the `US120.g4` grammar (ANTLR4). The objective is to guarantee that input files are not only structurally valid but also comply with business rules (dates, coordinates, angles, etc.).

We chose to add the aircraft and pilot information to the grammar to ensure that the code that we have regarding ESINF (US080, 081 and 085) stay in touch with the grammars that are validated by this US.

## File Structure

The grammar follows the `FlightPlan` aggregate. A flight plan declares, at the top level, the extracted flight id, the flight type, the assigned aircraft, the pilot and the route, followed by one or more legs:

```
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

## Validation Architecture

Validation is split into two strict phases. If the first phase fails, the second phase is automatically skipped to prevent processing corrupted parse trees.

1. **Lexical and Syntactic Validation (`US120ErrorListener`)**
    - Catches unrecognized characters.
    - Detects structural failures (e.g., missing braces, misspelled keywords).

2. **Semantic Validation (`US120SemanticVisitor`)**
    - Traverses the successfully built Parse Tree.
    - Validates domain-specific business rules.

## Implemented Semantic Rules

The `US120SemanticVisitor` enforces the following constraints:

* **File Extension:** The input file must have the `.dsldig5` extension.
* **Aircraft Registration (`visitRegistration`):** The assigned aircraft registration must not be empty.
* **Pilot Identifier (`visitPilotId`):** The pilot identifier must not be empty.
* **Dates (`visitDate`):** Validates if the day, month, and year form a legally existing calendar date via `LocalDate`.
* **Times (`visitTime`):** Ensures hours, minutes, and seconds are within standard bounds (0-23h, 0-59m, 0-59s).
* **Geographic Coordinates (`visitCoord`):**
    * **Latitude:** Must be between `-90` and `90`.
    * **Longitude:** Must be between `-180` and `180`.
* **Altitude Slots (`visitAltSlot`):** Enforces that the minimum altitude in a slot does not exceed its maximum altitude.
* **Wind Angle (`visitWindAngle`):** Ensures the wind direction angle belongs to the interval `[0, 360[`.
* **Positive Quantities:** Prevents physical magnitudes like Width (`visitWidth`) and Speed (`visitSpeed`) from having values less than or equal to `0`.

## How to Run

Compile and start the application as usual (`run.sh`, `run-client.sh`, or `mvn exec:java`). After login, open **Air Transport Company > Validate flight plan** and enter the path to a `.dsldig5` file (e.g. `docs/us_120/testFiles/exemplo.dsldig5`).

The ANTLR lexer, parser, visitor and listener are generated automatically during `mvn compile` from `src/main/antlr4/US120.g4`.