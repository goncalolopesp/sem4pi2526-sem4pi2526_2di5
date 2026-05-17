CC=gcc
CFLAGS=-Wall -Wextra -Wpedantic -std=c11 -D_DEFAULT_SOURCE -g
LDLIBS=-lm
SRC=main.c flight_process.c flight_plan_parser.c position_tracker.c safety_monitor.c
BIN=flight_simulator

all: $(BIN)

$(BIN): $(SRC)
	$(CC) $(CFLAGS) $(SRC) -o $(BIN) $(LDLIBS)

run: $(BIN)
	./$(BIN) --area 39 43 -9 -3 --start 0 --end 3600 --step 60 --weather clear --safety-min-sep 3 --safety-min-alt 300 --perf-max-flights 10 --flight data/Flight_Plan_v0a.json

clean:
	rm -f $(BIN) simulation_report.txt