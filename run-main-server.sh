#!/bin/bash
#
# Unified SEM4PI remote access TCP server (all US on the same port).
#
# Usage:
#   ./run-main-server.sh [port]
#
# Defaults: port=8044
#
set -e

PORT="${1:-}"

echo "Compiling project (Maven)..."
mvn -q compile
echo "Compilation finished."

echo "Building classpath..."
mvn -q dependency:build-classpath -Dmdep.outputFile=cp.txt
echo "Classpath ready."

echo "Starting SEM4PI Remote Access server..."
java -cp "target/classes:$(cat cp.txt)" pt.isep.sem4pi.MainServer ${PORT}
