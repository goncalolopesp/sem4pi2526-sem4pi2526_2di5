#!/bin/bash
#
# US44 - Starts the embedded Weather Person remote access TCP server.
#
# Usage:
#   ./run-weather-server.sh [port]
set -e

PORT="${1:-}"

mvn -q compile
mvn -q dependency:build-classpath -Dmdep.outputFile=cp.txt

echo "Starting SEM4PI Remote Access server (via MainServer)..."
java -cp "target/classes:$(cat cp.txt)" pt.isep.sem4pi.MainServer ${PORT}
