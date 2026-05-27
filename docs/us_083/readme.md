## Technical Overview

The grammar is designed to parse a flight planning Domain-Specific Language (DSL). It defines a nested structure where a top-level **Flight** contains one or more **Legs**, and each leg contains specific parameters such as departure, arrival, routing, segments, and fuel specifications.

The keywords are case-insensitive, but the structural block layout mimics common curly-brace programming languages.

---

## 1. Top-Level Structure

### `flightPlan : flight EOF ;`

This is the entry point of the parser. It specifies that a valid file must contain exactly one `flight` rule, followed immediately by the End of File (EOF) marker.

### `flight : FLIGHT ID flightType '{' leg+ '};' ;`

Defines the main container.

* It starts with the case-insensitive keyword `flight`.
* It is followed by an identifier (`ID`) representing the flight's unique designation.
* It requires a `flightType`.
* The body is wrapped in curly braces `{}` and must contain one or more (`+`) instances of the `leg` rule.
* Crucially, the closing brace requires a trailing semicolon: `};`.

### `flightType : REGULAR | CHARTER ;`

Restricts the classification of the flight to either the `REGULAR` or `CHARTER` keyword.

---

## 2. Leg Structure and Inner Blocks

### `leg : LEG '{' departure arrival route segment+ fuel '};' ;`

Each leg represents a distinct stage of the journey. It is introduced by the `LEG` keyword and enclosed in `{ };`. Inside, components must appear in a strict sequential order:

1. `departure` (Exactly one)
2. `arrival` (Exactly one)
3. `route` (Exactly one)
4. `segment` (One or more)
5. `fuel` (Exactly one)

### `departure : DEPARTURE '{' airport ',' date ',' time '};' ;`

Defines the origin of the leg. It requires an airport code, a date literal, and a time literal, all separated by commas inside curly braces ending with a semicolon.

### `arrival : ARRIVAL '{' airport ',' time '};' ;`

Defines the destination of the leg. Note that unlike the departure block, the arrival block only accepts an airport code and a time literal, omitting the date.

---

## 3. Routing, Segments, and Coordinates

### `route : ROUTE ID '{' routes+ '}' ';' ;`

Defines the general airway track. It takes an identifier (`ID`), followed by one or more `routes` (which resolve to coordinates) separated by whitespace. Unlike other blocks, this block ends with a standard `}` followed by a standalone semicolon `;` instead of `};`.

### `segment : SEGMENT '{' '(' beginNode ')' ',' '(' endNode ')' ',' '{' altSlot+ '}' ',' width ',' windAngle ',' speed '};' ;`

This is the most complex syntax block in the grammar. It specifies the physical properties of a flight segment. The parameters must follow this exact format:

1. `( beginNode )` wrapped in parentheses.
2. A comma, followed by `( endNode )` wrapped in parentheses.
3. A comma, followed by a list of one or more altitude slots enclosed in curly braces: `{ altSlot+ }`.
4. Commas separating the remaining numerical values: `width`, `windAngle`, and `speed`.

### Coordinate and Slot Processing

* `beginNode` / `endNode` / `routes`: These all resolve to the `coord` rule.
* `coord : '-'? number '|' '-'? number ;`: A coordinate consists of two numbers separated by a vertical pipe `|`. Each number can optionally be negative (`-'?`).
* `altSlot : '(' number ',' number ')' ;`: An altitude slot represents a range or boundary defined by two numbers separated by a comma and enclosed in parentheses, for example: `(10000,12000)`.

---

## 4. Helper Rules and Data Types

* `width`, `windAngle`, `speed`: These rules are semantic aliases for the `number` rule, used to make the parsing tree easier to read during downstream compiler phases.
* `fuel : FUEL '{' number ',' unit '};' ;`: Defines fuel configuration with a capacity (`number`) and a specific measurement configuration (`unit`).
* `airport : IATA | ICAO ;`: Restricts airport identifiers to 3-letter (IATA) or 4-letter (ICAO) tokens.
* `date` / `time`: Bind directly to their respective literal formats.
* `number : INT | FLOAT ;`: Simplifies number handling by matching either whole integers or decimal floats.
* `unit : UNIT_KG | UNIT_L | UNIT_M | UNIT_MS ;`: Limits allowed fuel/consumption units to `kg`, `l`, `m`, or `m/s`.

---

## 5. Lexer Rules (Tokens)

Lexer rules define the raw characters that make up the language elements.

### Case-Insensitive Keywords

Keywords like `FLIGHT`, `LEG`, `DEPARTURE`, `ARRIVAL`, `ROUTE`, `SEGMENT`, `FUEL`, `REGULAR`, and `CHARTER` use regular expressions matching both upper and lower case for every character (e.g., `[fF][lL]...`), ensuring the DSL is entirely case-insensitive for commands.

### Literal and Numerical Formats

* `DATE_LIT : [0-9]+ '/' [0-9]+ '/' [0-9]+ ;`: Matches digits separated by slashes. Note that it allows any number of digits per section (it does not enforce a strict 2-digit/4-digit rule at the lexer level).
* `TIME_LIT : [0-9]+ ':' [0-9]+ (':' [0-9]+)? ;`: Matches hours and minutes, making seconds optional `(':' [0-9]+)?`.
* `INT` and `FLOAT`: Standard numeric expressions. `FLOAT` strictly requires a decimal point surrounded by numbers on both sides.

### Identifiers and Ambiguity Resolution

* `IATA : [A-Za-z][A-Za-z][A-Za-z] ;` (Exactly 3 letters)
* `ICAO : [A-Za-z][A-Za-z][A-Za-z][A-Za-z] ;` (Exactly 4 letters)
* `ID : [a-zA-Z][a-zA-Z0-9]* ;` (Starts with a letter, followed by letters or digits)

> **Lexical Order Note:** In ANTLR4, lexer rules are evaluated in the order they are defined. Because `IATA` and `ICAO` are defined *before* `ID`, any 3-letter or 4-letter word encountered outside of a explicit keyword will be parsed as an airport code token rather than a generic identifier.

### White Space Handling

* `WS : [ \t\r\n]+ -> skip ;`: Instructs the lexer to discard spaces, tabs, carriage returns, and newlines, allowing the user to format the document with flexible indentation.