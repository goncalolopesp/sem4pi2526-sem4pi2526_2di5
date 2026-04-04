# Project AIControl

## 1. Description of the Project

**Project AIControl** (developed for the startup AlSafe) is a comprehensive flight management and air traffic simulation prototype designed to modernize obsolete flight control infrastructures. The system provides a scalable, multi-component architecture to manage, validate, and simulate complex aeronautical operations.

The project is built upon a Domain-Driven Design (DDD) architecture and is divided into several core operational modules:

* **Backoffice & Fleet Management:** A Java-based core system to manage essential aeronautical entities, including air control areas, airports, aircraft models, air transport companies, and user roles.
* **Flight Planning & DSL:** To avoid vendor lock-in, the system features a custom Domain Specific Language (Flight DSL) processed via ANTLR. This allows pilots and operators to formally define, syntactically validate, and safely import flight plans into the system.
* **High-Performance Simulation Engine:** A robust simulation engine developed in C that utilizes multi-processing, shared memory, and inter-process communication (IPC). It simulates real-world flight physics (lift, drag, fuel consumption), integrates real-time weather data, and continuously monitors the airspace to detect and report safety violations or potential collisions.
* **Monitoring & Visualization:** A dedicated UDP logging server and web interface that tracks and displays step-by-step flight progression and safety statuses in real-time.

By integrating these modules, AIControl serves as a robust proof-of-concept for the next generation of safe, parallelized, and data-driven air traffic control systems.

## 2. Planning and Technical Documentation

[Planning and Technical Documentation](docs/readme.md)

## 3. How to Build
If you're using a Windows System, jump to [here](#windows-environment)
## Unix subsystems

To build the application, you must run the build.sh script:

    ./build.sh

## 4. How to Execute Tests

To run the tests, you must run the test.sh script

    ./test.sh

## 5. How to Run

To run the application, you must run the run.sh script:

    ./run.sh

## 6. How to Install/Deploy into Another Machine (or Virtual Machine)

To deploy the application, you must first run the deploy.sh script. That script will create a Java Archive File, which you can run in your other Machine. You can run the deploy script with:

    ./deploy.sh

And you can execute the Java Archive File in your Terminal with:

    java -jar {name of the JAR file}
## 7. How to Generate PlantUML Diagrams

To generate plantuml diagrams for documentation execute the script:

    ./generate-plantuml-diagrams.sh


# Windows Environment

To build the application, you must run the build.bat script:

    ./build.bat

## 4. How to Execute Tests

To run the tests, you must run the test.bat script

    ./test.bat

## 5. How to Run

To run the application, you must run the run.bat script:

    ./run.bat

## 6. How to Install/Deploy into Another Machine (or Virtual Machine)

To deploy the application, you must first run the deploy.bat script. That script will create a Java Archive File, which you can run in your other Machine. You can run the deploy script with:

    ./deploy.bat

And you can execute the Java Archive File in your Command Prompt/Powershell with:

    java -jar {name of the JAR file}
## 7. How to Generate PlantUML Diagrams

*Currently, there is no implementation for the generation of PlantUML Diagrams for Windows* 