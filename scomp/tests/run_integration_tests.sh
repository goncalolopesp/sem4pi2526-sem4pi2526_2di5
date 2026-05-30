#!/bin/bash
set -e

echo "Building simulator..."
make

echo "Running integration tests..."

mkdir -p tests/reports

OUT1="tests/out_us100_single.txt"
REPORT1="tests/reports/us100_single.txt"

OUT2="tests/out_us100_threshold.txt"
REPORT2="tests/reports/us100_threshold.txt"

OUT3="tests/out_us105_shared.txt"
REPORT3="tests/reports/us105_shared.txt"

OUT4="tests/out_invalid_args.txt"

rm -f "$OUT1" "$OUT2" "$OUT3" "$OUT4"
rm -f "$REPORT1" "$REPORT2" "$REPORT3"

echo "Test 1: US100 single flight with simplified physics"

./flight_simulator \
  --area 39 43 -9 -3 \
  --start 0 \
  --end 120 \
  --step 10 \
  --weather clear \
  --safety-min-sep 3 \
  --safety-min-alt 300 \
  --violation-threshold 10 \
  --perf-max-flights 10 \
  --flight data/Flight_Plan_v0a.json \
  --output "$REPORT1" > "$OUT1" 2>&1

grep -q "Starting simulation with 1 flight(s)." "$OUT1"
grep -q "Safety thresholds: separation=3.00 NM, altitude=300.0 m, threshold=10." "$OUT1"
grep -q "Position Tracker Summary (1 flights)" "$OUT1"
grep -q "Simulation finished. Report saved in $REPORT1" "$OUT1"

test -f "$REPORT1"
grep -q "Total flights: 1" "$REPORT1"
grep -q "Violation threshold: 10" "$REPORT1"
grep -q "Flight 123456-" "$REPORT1"
grep -q "updates 13" "$REPORT1"
grep -q "t=20 lat=41.278935 lon=-8.670523 alt=309.00 status=1" "$REPORT1"
grep -q "US101 Position Tracker (1 entries)" "$REPORT1"
grep -q "US102 Safety Violations (2 shown / 2 total)" "$REPORT1"
grep -q "Simulation result: PASS" "$REPORT1"

echo "Test 2: US100 early termination by violation threshold"

./flight_simulator \
  --area 39 43 -9 -3 \
  --start 0 \
  --end 120 \
  --step 10 \
  --weather clear \
  --safety-min-sep 3 \
  --safety-min-alt 300 \
  --violation-threshold 2 \
  --perf-max-flights 10 \
  --flight data/Flight_Plan_v0a.json \
  --output "$REPORT2" > "$OUT2" 2>&1

grep -q "threshold=2" "$OUT2"
grep -q "Violation threshold (2) exceeded. Aborting simulation." "$OUT2"
grep -q "Simulation finished. Report saved in $REPORT2" "$OUT2"

test -f "$REPORT2"
grep -q "Violation threshold: 2" "$REPORT2"
grep -q "Simulation result: FAIL" "$REPORT2"

echo "Test 3: US105 shared memory with two concurrent flights"

./flight_simulator \
  --area 39 43 -9 -3 \
  --start 0 \
  --end 120 \
  --step 10 \
  --weather clear \
  --safety-min-sep 3 \
  --safety-min-alt 300 \
  --violation-threshold 50 \
  --perf-max-flights 10 \
  --flight data/Flight_Plan_v0a.json \
  --flight data/Flight_Plan_v0c.json \
  --output "$REPORT3" > "$OUT3" 2>&1

grep -q "Starting simulation with 2 flight(s)." "$OUT3"
grep -q "\[shared\] active=1 updates=" "$OUT3"
grep -q "\[shared\] active=2 updates=" "$OUT3"
grep -q "Position Tracker Summary (2 flights)" "$OUT3"
grep -q "Simulation finished. Report saved in $REPORT3" "$OUT3"

test -f "$REPORT3"
grep -q "Total flights: 2" "$REPORT3"
grep -q "Flight 123456-" "$REPORT3"
grep -q "Flight 654321-" "$REPORT3"
grep -q "US101 Position Tracker (2 entries)" "$REPORT3"
grep -q "US102 Safety Violations (4 shown / 4 total)" "$REPORT3"
grep -q "Simulation result: PASS" "$REPORT3"

echo "Test 4: invalid arguments"

set +e
./flight_simulator \
  --area 39 43 -9 -3 \
  --start 0 \
  --end 3600 \
  --step 60 \
  --weather clear \
  --safety-min-sep 3 \
  --safety-min-alt 300 \
  --violation-threshold 10 \
  --perf-max-flights 10 \
  --flight data/Flight_Plan_v0a.json data/Flight_Plan_v0a.json > "$OUT4" 2>&1
STATUS=$?
set -e

if [ "$STATUS" -eq 0 ]; then
  echo "Expected invalid argument test to fail"
  exit 1
fi

grep -q "Invalid argument" "$OUT4"

echo ""
echo "All integration tests passed"