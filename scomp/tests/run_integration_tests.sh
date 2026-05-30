#!/bin/bash
set -e

echo "Building simulator..."
make >/dev/null

echo "Running integration tests..."
echo ""

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

echo "[1/4] US100 single flight"

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

grep -Fq "Starting simulation with 1 flight(s)." "$OUT1"
grep -Fq "Safety thresholds: separation=3.00 NM, altitude=300.0 m, threshold=10." "$OUT1"
grep -Fq "Position Tracker Summary (1 flights)" "$OUT1"
grep -Fq "Simulation finished. Report saved in $REPORT1" "$OUT1"

test -f "$REPORT1"
grep -Fq "Total flights: 1" "$REPORT1"
grep -Fq "Violation threshold: 10" "$REPORT1"
grep -Fq "Flight 123456-" "$REPORT1"
grep -Fq "updates 13" "$REPORT1"
grep -Fq "US101 Position Tracker (1 entries)" "$REPORT1"
grep -Fq "US102 Safety Violations (2 shown / 2 total)" "$REPORT1"
grep -Fq "Simulation result: PASS" "$REPORT1"

echo "[2/4] Threshold termination"

./flight_simulator \
  --area 39 43 -9 -3 \
  --start 0 \
  --end 20 \
  --step 1 \
  --weather clear \
  --safety-min-sep 3 \
  --safety-min-alt 300 \
  --violation-threshold 2 \
  --perf-max-flights 10 \
  --flight data/Flight_Plan_v0a.json \
  --output "$REPORT2" > "$OUT2" 2>&1

grep -Fq "threshold=2" "$OUT2"
grep -Fq "Violation threshold (2) exceeded. Aborting simulation." "$OUT2"
grep -Fq "Simulation finished. Report saved in $REPORT2" "$OUT2"

test -f "$REPORT2"
grep -Fq "Violation threshold: 2" "$REPORT2"
grep -Fq "Simulation result: FAIL" "$REPORT2"

echo "[3/4] US105 concurrent flights"

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

grep -Fq "Starting simulation with 2 flight(s)." "$OUT3"
grep -Fq "[shared] active=1 updates=" "$OUT3"
grep -Fq "[shared] active=2 updates=" "$OUT3"
grep -Fq "Position Tracker Summary (2 flights)" "$OUT3"
grep -Fq "Simulation finished. Report saved in $REPORT3" "$OUT3"

test -f "$REPORT3"
grep -Fq "Total flights: 2" "$REPORT3"
grep -Fq "Flight 123456-" "$REPORT3"
grep -Fq "Flight 654321-" "$REPORT3"
grep -Fq "US101 Position Tracker (2 entries)" "$REPORT3"
grep -Fq "US102 Safety Violations (4 shown / 4 total)" "$REPORT3"
grep -Fq "Simulation result: PASS" "$REPORT3"

echo "[4/4] Invalid arguments"

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

grep -Fq "Invalid argument" "$OUT4"

echo ""
echo "✓ All integration tests passed"