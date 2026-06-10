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

echo === US090 - Remote Access Logging Server ===
echo Port: %PORT%
echo Log file: %LOG_FILE%
echo.

echo [1/3] Compiling project...
call mvn -q compile
if errorlevel 1 exit /b 1
echo [1/3] Compilation complete.
echo.

echo [2/3] Generating classpath...
call mvn -q dependency:build-classpath -Dmdep.outputFile=cp.txt
if errorlevel 1 exit /b 1
echo [2/3] Classpath ready.
echo.

echo [3/3] Starting Remote Access Logging Server on UDP port %PORT%...
echo       Writing log entries to: %LOG_FILE%
echo       Press Ctrl+C to stop.
for /f "delims=" %%i in (cp.txt) do set CP=%%i
java -cp "target/classes;%CP%" pt.isep.sem4pi.presentation.network.remoteaccess.RemoteAccessLoggingServer "%PORT%" "%LOG_FILE%"
