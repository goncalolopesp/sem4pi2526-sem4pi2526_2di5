# US83 - Flight DSL Specification and Validation

---

## User Story Description

This User Story will validate Flight Descriptions, following some rules specified.

The Flight Descriptions of this project should follow the following example:

```
flight ID Type {
	leg {
		departure {
			IATA | ICAO code,
			DD/MM/YYYY,
			HH:mm:ss
		};
		arrival {
			IATA | ICAO code,
			HH:mm:ss
		};
		route ID;
		segment {
			(Start node x | Start node y),
			(End node x | End node y),
			altitude slots,
			width for each altitude slots,
			Wind angle,
			Speed
		};
		fuel {
			Capacity,
			kg | l | m | m/s
		};
	};
};
```
This can be found in [this file](examples/exampleFile.md);

## Validation Rules

 - The Flight ID is a String composed of 1 or more characters: The first character must be a letter, while the other characters can either be letters or digits.
 - The Flight Type can be either REGULAR or CHARTER.
 - The departure and arrival Airport codes (IATA or ICAO) are String composed of 3 or 4 letters, respectively.
 - The date is in the format DD/MM/YYYY, while the time is in the format HH:mm:ss.
 - The Route ID is a String composed of 1 or more characters: The first character must be a letter, while the other characters can either be letters or digits.
 - The Start and End node coordinates are in the format (x | y), where x and y are integers or floats.
 - The altitude slots, width for each altitude slots, Wind angle and Speed are integers or floats.
 - The fuel Capacity is an integer or float, while the fuel unit can be either kg, l, m or m/s.

## Requirements
To run this User Story, you need:
- ANTLR4
- Java 17