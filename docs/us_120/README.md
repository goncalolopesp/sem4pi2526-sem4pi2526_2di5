# US120 - Flight Plan Validation (ANTLR4)

This project implements a syntactic and semantic analyzer for flight plan files using the `US120.g4` grammar (ANTLR4). The objective is to guarantee that input files are not only structurally valid but also comply with business rules (dates, coordinates, angles, etc.).

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
* **Dates (`visitDate`):** Validates if the day, month, and year form a legally existing calendar date via `LocalDate`.
* **Times (`visitTime`):** Ensures hours, minutes, and seconds are within standard bounds (0-23h, 0-59m, 0-59s).
* **Geographic Coordinates (`visitCoord`):**
    * **Latitude:** Must be between `-90` and `90`.
    * **Longitude:** Must be between `-180` and `180`.
* **Altitude Slots (`visitAltSlot`):** Enforces that the minimum altitude in a slot does not exceed its maximum altitude.
* **Wind Angle (`visitWindAngle`):** Ensures the wind direction angle belongs to the interval `[0, 360[`.
* **Positive Quantities:** Prevents physical magnitudes like Width (`visitWidth`) and Speed (`visitSpeed`) from having values less than or equal to `0`.

## How to Run

### 1. Generate ANTLR Files (Lexer, Parser, Visitor, Listener)
Ensure the ANTLR4 tool is properly configured in your environment. Run the following command in the directory containing the `US120.g4` file:
```bash
java org.antlr.v4.Tool -visitor -listener US120.g4
```
Then, `cd` to the project's root and run:
```bash
mvn generate-sources compile exec:java@us120 -Pus120 -Dexec.args="docs/us_120/testFiles/exemplo.dsldig5"
```
The argument in the `-Dexec.args=` is the path to the file. In this case, I'm testing the example file created for the US.