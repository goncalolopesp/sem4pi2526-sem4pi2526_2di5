#!/bin/bash

mvn clean compile
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt

echo "Finished compiling, trying to run"

java -cp target/classes:$(cat cp.txt) pt.isep.sem4pi.Main