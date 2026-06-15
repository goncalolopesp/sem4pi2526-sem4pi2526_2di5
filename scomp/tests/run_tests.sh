#!/bin/bash

set -e

echo "Compiling tests..."

# DEFESA:
# Cada binário liga apenas o módulo sob teste e as dependências mínimas, mantendo os testes unitários isolados.
gcc -Wall -Wextra -Wpedantic -std=c11 -D_DEFAULT_SOURCE \
tests/test_position_tracker.c \
src/position_tracker.c \
-o tests/test_position_tracker \
-lm

gcc -Wall -Wextra -Wpedantic -std=c11 -D_DEFAULT_SOURCE \
tests/test_safety_monitor.c \
src/position_tracker.c \
src/safety_monitor.c \
-o tests/test_safety_monitor \
-lm

gcc -Wall -Wextra -Wpedantic -std=c11 -D_DEFAULT_SOURCE \
tests/test_shared_simulation.c \
src/shared_simulation.c \
-o tests/test_shared_simulation \
-lm -pthread

gcc -Wall -Wextra -Wpedantic -std=c11 \
-I src \
tests/test_step_synchronization.c \
src/shared_simulation.c \
-o tests/test_step_synchronization \
-pthread -lrt

gcc -Wall -Wextra -Wpedantic -std=c11 -D_DEFAULT_SOURCE \
tests/test_simulation_threads.c \
src/simulation_threads.c \
src/safety_monitor.c \
src/position_tracker.c \
-o tests/test_simulation_threads \
-lm -pthread

echo ""

# DEFESA:
# set -e transforma qualquer assert ou exit code de falha numa falha imediata da suite.
./tests/test_position_tracker
./tests/test_safety_monitor
./tests/test_shared_simulation
./tests/test_step_synchronization
./tests/test_simulation_threads

echo ""
echo "All tests passed"
