#!/bin/bash
set -e

echo "Building simulator..."
make

echo "Running integration tests..."

OUT1="tests/out_single_flight.txt"
OUT2="tests/out_two_flights.txt"
OUT3="tests/out_invalid_args.txt"

./flight_simulator \
  --area 39 43 -9 -3 \
  --start 0 \
  --end 3600 \
  --step 60 \
  --flight data/Flight_Plan_v0a.json > "$OUT1" 2>&1

grep -q "Starting simulation with 1 flight(s)." "$OUT1"
grep -q "history=61 pts" "$OUT1"
grep -q "Simulation finished. Report saved in simulation_report.txt" "$OUT1"
test -f simulation_report.txt

./flight_simulator \
  --area 38 43 -10 3 \
  --start 0 \
  --end 3600 \
  --step 60 \
  --flight data/Flight_Plan_v0a.json \
  --flight data/Flight_Plan_v0c.json > "$OUT2" 2>&1

grep -q "Starting simulation with 2 flight(s)." "$OUT2"
grep -q "Position Tracker Summary (2 flights)" "$OUT2"
grep -q "Simulation finished. Report saved in simulation_report.txt" "$OUT2"

set +e
./flight_simulator \
  --area 39 43 -9 -3 \
  --start 0 \
  --end 3600 \
  --step 60 \
  --flight data/Flight_Plan_v0a.json data/Flight_Plan_v0a.json > "$OUT3" 2>&1
STATUS=$?
set -e

if [ "$STATUS" -eq 0 ]; then
  echo "Expected invalid argument test to fail"
  exit 1
fi

grep -q "Invalid argument" "$OUT3"

echo "All integration tests passed"