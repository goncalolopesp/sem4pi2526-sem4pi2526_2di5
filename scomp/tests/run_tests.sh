#!/bin/bash

set -e

echo "Compiling tests..."

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

echo ""

./tests/test_position_tracker
./tests/test_safety_monitor
./tests/test_shared_simulation

echo ""
echo "All tests passed"