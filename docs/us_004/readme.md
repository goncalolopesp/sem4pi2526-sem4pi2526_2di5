# US004 - Continuous Integration Server

## 1. Context

This task corresponds to the initial setup of the Continuous Integration process for the project.

The team follows a workflow based on:
- individual branches per issue
- integration into `development`
- final integration into `main`

For that reason, it is important to validate the system automatically throughout the whole development process.

This task is being implemented for the first time in the project.

---

## 2. Requirements

**US004**  
As Project Manager, I want the team to setup a continuous integration server.

GitHub Actions/Workflows should be used.

---

## 3. Solution

### 3.1 Overview

A GitHub Actions workflow was created to automatically build and test the project whenever changes are pushed to the repository.

The workflow ensures that:
- the project compiles successfully
- all tests are executed automatically
- code coverage is generated
- failures are immediately detected

---

### 3.2 Technologies Used

- **Build Tool:** Maven
- **Testing Framework:** JUnit 4.13.2
- **Code Coverage:** JaCoCo
- **CI Tool:** GitHub Actions

---

### 3.3 Workflow Location

```text
.github/workflows/ci.yml