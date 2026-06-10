#!/bin/bash
#
# US44 - Starts the Weather Person remote access TCP client.
#
# Usage:
#   ./run-client.sh [host] [port]
#
# Defaults: host=127.0.0.1, port=8044
#
set -e

HOST="${1:-127.0.0.1}"
PORT="${2:-8044}"

mvn -q compile
mvn -q dependency:build-classpath -Dmdep.outputFile=cp.txt

echo "Connecting to weather server at ${HOST}:${PORT}..."
java -cp "target/classes:$(cat cp.txt)" pt.isep.sem4pi.client.weather.WeatherClientApp "${HOST}" "${PORT}"
