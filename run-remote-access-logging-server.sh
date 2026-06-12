#!/bin/bash
#
# US090 - Starts the Remote Access Logging Server.
#
# Usage:
#   ./run-remote-access-logging-server.sh [udpPort] [logFile] [httpPort]
set -e

PORT="${1:-9000}"
LOG_FILE="${2:-remote-access.log}"
HTTP_PORT="${3:-8081}"

echo "=== US090 - Remote Access Logging Server ==="
echo "Port: ${PORT}"
echo "Log file: ${LOG_FILE}"
echo "HTTP port: ${HTTP_PORT}"
echo

echo "[1/3] Compiling project..."
mvn -q compile
echo "[1/3] Compilation complete."
echo

echo "[2/3] Generating classpath..."
mvn -q dependency:build-classpath -Dmdep.outputFile=cp.txt
echo "[2/3] Classpath ready."
echo

echo "[3/3] Starting Remote Access Logging Server on UDP port ${PORT}..."
echo "      Writing log entries to: ${LOG_FILE}"
echo "      Status pages: http://localhost:${HTTP_PORT}/"
echo "      Press Ctrl+C to stop."
java -cp "target/classes:$(cat cp.txt)" pt.isep.sem4pi.presentation.network.remoteaccess.RemoteAccessLoggingServer "${PORT}" "${LOG_FILE}" "${HTTP_PORT}"
