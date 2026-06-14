# US081 - Create a Flight Plan From a File

---

## Description

This User Story validates and imports flight plans from files using ANTLR4.

The solution reuses the Core Flight DSL defined in US120 (`src/main/antlr4/US120.g4`).

---

## Features

- Lexical validation
- Syntactic validation
- Semantic validation
- Error reporting
- Mapping of aircraft, pilot, route, legs and segments into the domain model

---

## Requirements

To run this User Story, you need:

- Java 17
- ANTLR4

---

## Files

- `US81.g4` (documentation reference aligned with `US120.g4`)
- `exampleValid.md`
- `exampleInvalid.md`
