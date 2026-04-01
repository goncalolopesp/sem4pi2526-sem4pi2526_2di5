# Project AIControl

## 1. Description of the Project

*To Do*

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