#!/bin/bash
#
# US78 - Starts the Air Transport Company Collaborator remote access TCP client.
#
# Usage:
#   ./run-atcc-client.sh [host] [port]
#
# Defaults: host=127.0.0.1, port=8044
#
set -e

HOST="${1:-127.0.0.1}"
PORT="${2:-8044}"

echo "Compiling project..."
mvn -q compile
echo "Compilation finished."

echo "Building classpath..."
mvn -q dependency:build-classpath -Dmdep.outputFile=cp.txt
echo "Classpath ready."

echo "Starting Air Transport Company client and connecting to ${HOST}:${PORT}..."
java -cp "target/classes:$(cat cp.txt)" pt.isep.sem4pi.client.airtransportcompany.AirTransportCompanyClientApp "${HOST}" "${PORT}"
