#!/bin/bash
#
# US090 - Starts the Remote Access Logging Server.
#
# Usage:
#   ./run-remote-access-logging-server.sh [port] [logFile]
set -e

PORT="${1:-9000}"
LOG_FILE="${2:-remote-access.log}"

mvn -q compile
mvn -q dependency:build-classpath -Dmdep.outputFile=cp.txt

echo "Starting Remote Access Logging Server on UDP port ${PORT} ..."
java -cp "target/classes:$(cat cp.txt)" pt.isep.sem4pi.presentation.network.remoteaccess.RemoteAccessLoggingServer "${PORT}" "${LOG_FILE}"