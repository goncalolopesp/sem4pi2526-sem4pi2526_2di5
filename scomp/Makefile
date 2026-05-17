CC=gcc
CFLAGS=-Wall -Wextra -Wpedantic -std=c11 -D_DEFAULT_SOURCE -g
SRC=src/main.c src/flight_process.c src/flight_plan_parser.c src/position_tracker.c
BIN=us100_simulator

all: $(BIN)

$(BIN): $(SRC)
	$(CC) $(CFLAGS) $(SRC) -o $(BIN)

run: $(BIN)
	./$(BIN) --area 39 43 -9 -3 --start 0 --end 3600 --step 60 --flight data/Flight_Plan_v0a.json

clean:
	rm -f $(BIN) simulation_report.txt
