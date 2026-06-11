#ifndef FLIGHT_TEST_H
#define FLIGHT_TEST_H

#include "simulation.h"

typedef struct
{
    int passed;
    int altitude_violations;
    int fuel_violation;
    double total_distance_nm;
    double fuel_consumed_kg;
    double fuel_remaining_kg;
    char summary[512];
} FlightTestResult;

int run_flight_test(const char *plan_path,
                    double min_alt_m,
                    double min_reserve_fuel_kg,
                    int step_seconds,
                    FlightTestResult *result,
                    char *err,
                    size_t err_size);

#endif
