#!/bin/bash

set -e

echo "Compiling tests..."

# Compila o teste do PositionTracker com o módulo de produção necessário.
gcc -Wall -Wextra -Wpedantic -std=c11 -D_DEFAULT_SOURCE \
tests/test_position_tracker.c \
src/position_tracker.c \
-o tests/test_position_tracker \
-lm

# Compila o teste do monitor de segurança com tracker e deteção de violações.
gcc -Wall -Wextra -Wpedantic -std=c11 -D_DEFAULT_SOURCE \
tests/test_safety_monitor.c \
src/position_tracker.c \
src/safety_monitor.c \
-o tests/test_safety_monitor \
-lm

# Compila o teste da memória partilhada com pthreads para sincronização.
gcc -Wall -Wextra -Wpedantic -std=c11 -D_DEFAULT_SOURCE \
tests/test_shared_simulation.c \
src/shared_simulation.c \
-o tests/test_shared_simulation \
-lm -pthread

# Compila o teste passo-a-passo que usa semáforos entre processos.
gcc -Wall -Wextra -Wpedantic -std=c11 \
-I src \
tests/test_step_synchronization.c \
src/shared_simulation.c \
-o tests/test_step_synchronization \
-pthread -lrt

# Compila o teste de coordenação produtor-consumidor entre threads.
gcc -Wall -Wextra -Wpedantic -std=c11 -D_DEFAULT_SOURCE \
tests/test_simulation_threads.c \
src/simulation_threads.c \
src/safety_monitor.c \
src/position_tracker.c \
-o tests/test_simulation_threads \
-lm -pthread

echo ""

# Corre os testes unitários antes dos testes de integração.
./tests/test_position_tracker
./tests/test_safety_monitor
./tests/test_shared_simulation
./tests/test_step_synchronization
./tests/test_simulation_threads

echo ""
echo "All tests passed"
