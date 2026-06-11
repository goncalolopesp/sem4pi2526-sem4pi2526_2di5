#define _POSIX_C_SOURCE 200809L

#include "flight_test.h"

#include <math.h>
#include <stdio.h>
#include <string.h>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

static Position interpolate(Position a, Position b, double ratio)
{
    Position p;
    p.latitude = a.latitude + (b.latitude - a.latitude) * ratio;
    p.longitude = a.longitude + (b.longitude - a.longitude) * ratio;
    p.altitude_m = a.altitude_m + (b.altitude_m - a.altitude_m) * ratio;
    return p;
}

static double distance_nm(Position a, Position b)
{
    double avg_lat_rad = ((a.latitude + b.latitude) / 2.0) * M_PI / 180.0;
    double dlat_nm = (b.latitude - a.latitude) * 60.0;
    double dlon_nm = (b.longitude - a.longitude) * 60.0 * cos(avg_lat_rad);
    return sqrt(dlat_nm * dlat_nm + dlon_nm * dlon_nm);
}

static double selected_speed_knots(const FlightPlan *plan, const Segment *seg)
{
    if (strcmp(seg->mode, "climb") == 0)
        return plan->climb_speed_knots;
    if (strcmp(seg->mode, "descend") == 0)
        return plan->descend_speed_knots;
    return plan->cruise_speed_knots;
}

static double selected_vertical_rate_ms(const FlightPlan *plan, const Segment *seg)
{
    if (strcmp(seg->mode, "climb") == 0)
        return plan->climb_rate_ms;
    if (strcmp(seg->mode, "descend") == 0)
        return plan->descend_rate_ms;
    return 0.0;
}

static double fuel_burn_rate_kg_per_nm(const Segment *seg)
{
    if (strcmp(seg->mode, "climb") == 0)
        return 4.5;
    if (strcmp(seg->mode, "descend") == 0)
        return 2.5;
    return 3.0;
}

static int mode_requires_min_altitude(const Segment *seg)
{
    return strcmp(seg->mode, "cruise") == 0;
}

int run_flight_test(const char *plan_path,
                    double min_alt_m,
                    double min_reserve_fuel_kg,
                    int step_seconds,
                    FlightTestResult *result,
                    char *err,
                    size_t err_size)
{
    if (!plan_path || !result)
    {
        if (err && err_size > 0)
            snprintf(err, err_size, "Invalid arguments for flight test");
        return -1;
    }

    if (step_seconds <= 0)
        step_seconds = 60;

    memset(result, 0, sizeof(*result));

    FlightPlan plan;
    if (parse_flight_plan(plan_path, &plan, err, err_size) != 0)
        return -1;

    if (plan.fuel_kg <= 0.0)
    {
        snprintf(err, err_size, "Flight plan '%s' has no fuel quantity", plan_path);
        return -1;
    }

    double fuel_consumed = 0.0;
    double total_distance = 0.0;

    for (int i = 0; i < plan.segment_count; ++i)
    {
        Segment *seg = &plan.segments[i];
        double speed_knots = selected_speed_knots(&plan, seg);
        if (speed_knots <= 0.0)
            speed_knots = 1.0;

        double segment_distance_nm = distance_nm(seg->start, seg->end);
        total_distance += segment_distance_nm;
        fuel_consumed += segment_distance_nm * fuel_burn_rate_kg_per_nm(seg);

        double segment_seconds = (segment_distance_nm / speed_knots) * 3600.0;
        double vertical_rate = selected_vertical_rate_ms(&plan, seg);
        double altitude_delta = seg->end.altitude_m - seg->start.altitude_m;

        if (vertical_rate != 0.0)
        {
            double vertical_seconds = altitude_delta / vertical_rate;
            if (vertical_seconds < 0.0)
                vertical_seconds = -vertical_seconds;
            if (vertical_seconds > segment_seconds)
                segment_seconds = vertical_seconds;
        }

        int segment_steps = (int)(segment_seconds / step_seconds);
        if (segment_steps <= 0)
            segment_steps = 1;

        for (int s = 0; s <= segment_steps; ++s)
        {
            double elapsed = (double)s * (double)step_seconds;
            double ratio = elapsed / segment_seconds;
            if (ratio > 1.0)
                ratio = 1.0;

            Position p = interpolate(seg->start, seg->end, ratio);

            if (vertical_rate != 0.0)
            {
                double altitude = seg->start.altitude_m + vertical_rate * elapsed;
                if (altitude_delta >= 0.0 && altitude > seg->end.altitude_m)
                    altitude = seg->end.altitude_m;
                if (altitude_delta < 0.0 && altitude < seg->end.altitude_m)
                    altitude = seg->end.altitude_m;
                p.altitude_m = altitude;
            }

            if (mode_requires_min_altitude(seg) && p.altitude_m < min_alt_m)
                result->altitude_violations++;
        }
    }

    result->total_distance_nm = total_distance;
    result->fuel_consumed_kg = fuel_consumed;
    result->fuel_remaining_kg = plan.fuel_kg - fuel_consumed;

    if (result->fuel_remaining_kg < min_reserve_fuel_kg)
        result->fuel_violation = 1;

    result->passed = (result->altitude_violations == 0 && result->fuel_violation == 0);

    snprintf(result->summary, sizeof(result->summary),
             "Flight test %s: distance=%.1f nm, fuel consumed=%.1f kg, remaining=%.1f kg, "
             "altitude violations=%d, min reserve fuel=%.1f kg",
             result->passed ? "PASSED" : "FAILED",
             result->total_distance_nm,
             result->fuel_consumed_kg,
             result->fuel_remaining_kg,
             result->altitude_violations,
             min_reserve_fuel_kg);

    return 0;
}
