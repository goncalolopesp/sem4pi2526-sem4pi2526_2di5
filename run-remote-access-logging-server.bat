@echo off
rem
rem US090 - Starts the Remote Access Logging Server.
rem
rem Usage:
rem   run-remote-access-logging-server.bat [port] [logFile]

set PORT=%1
if "%PORT%"=="" set PORT=9000

set LOG_FILE=%2
if "%LOG_FILE%"=="" set LOG_FILE=remote-access.log

call mvn -q compile
call mvn -q dependency:build-classpath -Dmdep.outputFile=cp.txt

echo Starting Remote Access Logging Server on UDP port %PORT% ...
for /f "delims=" %%i in (cp.txt) do set CP=%%i
java -cp "target/classes;%CP%" pt.isep.sem4pi.presentation.network.remoteaccess.RemoteAccessLoggingServer "%PORT%" "%LOG_FILE%"