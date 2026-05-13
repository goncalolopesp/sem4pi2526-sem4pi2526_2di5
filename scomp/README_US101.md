# US101 - Capture and process flight movements

## Objective

C Implementation of US101. The component extends the main process of US100 with a dedicated module for position tracking (`position_tracker.c`), which receives `PositionUpdate` messages sent by the flight processes and maintains, for each aircraft, the current position and a complete history of past positions.

## Relation to Acceptance Criteria

- Each flight process sends position updates to the main process via pipe (inherited from US100).
- The main process tracks the current position of each aircraft through the `PositionTracker` structure.
- The system stores the history of past positions per flight in a dynamic structure (`TrackEntry`), growing by duplication for amortized efficiency.

## Added Module

The file `position_tracker.c` implements all tracking logic:

- `tracker_init` / `tracker_destroy`: lifecycle of the tracking table.
- `tracker_update`: called for each update received via pipe; creates the flight entry if it's the first message, updates the current position and adds the point to the history.
- `tracker_get_current`: query of the current position of a flight by `flight_id`.
- `tracker_print_summary`: prints the status of all tracked flights at the end of the simulation.

## Compile

```bash
make
```

## Run example

```bash
make run
```

Or manually:

```bash
./us101_simulator --area 39 43 -9 -3 --start 0 --end 3600 --step 60 --flight data/Flight_Plan_v0a.json
```

## Parameters

```bash
--area <minLat> <maxLat> <minLon> <maxLon>
--start <seconds>
--end <seconds>
--step <seconds>
--flight <file>
```

It is possible to specify multiple --flight, with one process created for each file.

## Output

The simulator outputs the current position and history of each tracked flight at each time step.
At the end of the simulation, it creates the file:

```text
simulation_report.txt
```

with the position history per flight, including a dedicated section for the PositionTracker summary.

## Integration Note

This implementation focuses on US101. The position history stored in TrackEntry serves as a basis for detecting conflicts between aircraft, which belongs to US102. Global synchronization by time step belongs to US103. The structure created here already lays the foundation for those US.