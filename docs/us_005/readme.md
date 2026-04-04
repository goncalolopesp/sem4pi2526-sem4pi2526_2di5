# US 005 - Automated Deployment

## 1. Context

This is the first time this task is assigned. The goal is to provide a set of automation scripts to streamline the development lifecycle (cleaning, building, testing, running, and deploying) of the system. While the primary requirement specifies Unix compatibility, the team also developed parallel scripts for Windows environments to support local development.

### 1.1 List of issues

**Analysis:** Identify the core tasks that require automation during the development and deployment phases.

**Design:** Define the commands and sequence of execution for each task across both Unix (Bash) and Windows (Batch) environments.

**Implement:** Create the `.sh` and `.bat` files for building, testing, running, deploying, and cleaning the project.

**Test:** Execute each script in its respective environment to ensure correct behavior and error handling.


## 2. Requirements

**US 005** As Project Manager, I want the team to add to the project the necessary scripts, so that build/executions/deployments can be executed effortlessly in a Unix compatible machine. Include scripts for all the major tasks and execution of applications.

**Acceptance Criteria:**

- AC 1 The system must include scripts for building the project.
- AC 2 The system must include scripts for executing the applications.
- AC 3 The system must include scripts for deploying the applications.
- AC 4 The scripts must be executable without effort on a Unix compatible machine.

**Dependencies/References:**

This requirement directly relates to **NFR07 - Deployment and Scripts**, which mandates that the repository includes the necessary scripts to build and deploy the solution in a Linux system.


## 3. Analysis

To ensure an effortless developer experience and reliable deployments, the team mapped the standard Java/Maven lifecycle commands to simple shell execution scripts.

The identified required actions were:
1. **Clean:** Remove old compiled binaries to prevent cache issues.
2. **Build:** Compile the source code.
3. **Test:** Run the automated unit test suite.
4. **Deploy:** Package the application into a distributable format (JAR).
5. **Run:** Chain the clean and build processes, then start the main application.

To accommodate different operating systems used by the development team, every Unix shell script (`.sh`) was paired with a corresponding Windows batch file (`.bat`).


## 4. Design

The design of this feature relies on OS-level scripting rather than application code.

### 4.1. Realization

*No class diagram is applicable for this US.* The execution flow for the most complex script, `run.sh`, follows this sequence:
1. Triggers `clean.sh` -> Removes `./out` and `./target` directories.
2. Triggers `build.sh` -> Finds all `.java` files and compiles them into `target/classes`.
3. Executes the application using `java -cp target/classes Main`.

### 4.2. Acceptance Tests

The validation of this functionality relies on manual execution tests across different terminals (e.g., Git Bash, WSL, Command Prompt, PowerShell).

**Test 1:** *Verifies that the build script compiles the code successfully.*
- Open a Unix terminal.
- Run `./build.sh`.
- Check if the `target/classes` directory is populated with `.class` files.

**Test 2:** *Verifies that the deploy script generates the executable JAR.*
- Open a Unix terminal.
- Run `./deploy.sh`.
- Check if a `.jar` file is created inside the `/target/` directory and the success echo is printed.


## 5. Implementation

The implementation consists of the following script files added to the project root:

**Unix Scripts (`.sh`):**
* `clean.sh`: Uses `rm -rf` to clear the `out` and `target` directories.
* `build.sh`: Uses `javac` to compile all `.java` files found in the `src` tree.
* `test.sh`: Executes `mvn test`.
* `deploy.sh`: Executes `mvn clean` followed by `mvn package` to generate the JAR file.
* `run.sh`: Chains the execution of `clean.sh` and `build.sh`, then starts the Java application.

**Windows Scripts (`.bat`):**
* `build.bat`: Recreates the `out` directory and recursively compiles `.java` files.
* `test.bat`: Executes `mvn test`.
* `deploy.bat`: Uses Maven to clean and package the project.
* `run.bat`: Calls `build.bat` and runs the compiled `Main` class.

*Note: The Unix scripts must be granted execution permissions before running using `chmod +x *.sh`.*


## 6. Integration/Demonstration

These scripts are integrated into the root directory of the repository. They serve as the primary entry points for interacting with the application during development and demonstration phases.

To demonstrate the application:
1. Open a terminal in the root directory.
2. Execute `./run.sh` (Unix) or `run.bat` (Windows).
3. The system will automatically clean old artifacts, compile the latest source code, and launch the `Main` class.


## 7. Observations

While the requirement specifically asks for Unix compatibility, providing `.bat` scripts ensures that team members developing on native Windows machines do not face friction.

It is noted that `build.sh` uses standard `javac` while `deploy.sh` utilizes `mvn`. As the project scales and relies more heavily on Maven for dependency management (as per NFR05), the `build.sh` script should eventually be refactored to use `mvn compile` instead of direct `javac` calls to ensure third-party dependencies are properly resolved.