# US085 - Test / Validate Flight Plan

## User Story

As **Pilot**, I want to test/validate a flight plan I have created.

## Acceptance Criteria

- Validation includes the stored flight plan data and a **C flight test**.
- The C test component verifies altitude and fuel requirements.
- On success the flight plan status becomes **VALIDATED**; on failure **REJECTED**.

## Architecture

```
Pilot UI  →  TestFlightPlanController  →  TestFlightPlanService
                                              ├─ FlightPlanDomainValidator
                                              ├─ FlightPlanJsonExporter (auto JSON)
                                              └─ CFlightTestRunner → flight_test_validator (C)
```

## Flow

1. Select a **DRAFT** flight plan (created via US080 or imported via US081).
2. The system re-validates the associated `.dsldig5` file with `US120FlightPlanValidator` (listener + visitor).
3. Java generates `scomp/data/generated/<plan-id>.json` from the DSL legs/segments (with liter-to-kg conversion when needed).
4. The C test runs using the destination airport's minimum reserve fuel rule.
5. Flight plan status becomes **VALIDATED** or **REJECTED**.

No DSL or JSON file path is requested from the user. Plans imported from DSL were already validated during import (US081/US120).

## C Component

Executable: `scomp/flight_test_validator`

```bash
cd scomp && make
./flight_test_validator \
  --plan data/generated/FP-000003.json \
  --min-alt 300 \
  --min-reserve-fuel 1200 \
  --step 60 \
  --output reports/test-report.txt
```

## Java Integration

- **Service:** `TestFlightPlanService`
- **Controller:** `TestFlightPlanController` (roles `PILOT`, `ADMIN`)
- **UI:** `TestFlightPlanUI` under **Flight Planning > Test / validate flight plan**

## Related Stories

- **US080:** create flight plan (starts in `DRAFT`).
- **US081:** import flight plan from DSL file (validation at import time).
- **US082:** adding weather data invalidates a previous test.

## Manual Test

1. Build C validator: `cd scomp && make`
2. Run application and login as `pilot` / `Password1`
3. Import or create a flight plan
4. Open **Flight Planning > Test / validate flight plan**
5. Select the draft plan — no extra files required
