#define _POSIX_C_SOURCE 200809L

#include "flight_test.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static void flight_test_print_usage(const char *program)
{
    fprintf(stderr,
            "Usage: %s --plan <json-file> --min-alt <meters> --min-reserve-fuel <kg> "
            "[--step <seconds>] [--output <report-file>]\n",
            program);
}

static int parse_double_arg(int argc, char **argv, int index, double *value)
{
    if (index + 1 >= argc)
        return -1;
    char *end = NULL;
    *value = strtod(argv[index + 1], &end);
    if (end == argv[index + 1])
        return -1;
    return 0;
}

static int parse_int_arg(int argc, char **argv, int index, int *value)
{
    if (index + 1 >= argc)
        return -1;
    char *end = NULL;
    long parsed = strtol(argv[index + 1], &end, 10);
    if (end == argv[index + 1])
        return -1;
    *value = (int)parsed;
    return 0;
}

int main(int argc, char **argv)
{
    const char *plan_path = NULL;
    const char *output_path = NULL;
    double min_alt_m = 300.0;
    double min_reserve_fuel_kg = 0.0;
    int step_seconds = 60;

    for (int i = 1; i < argc; ++i)
    {
        if (strcmp(argv[i], "--plan") == 0 && i + 1 < argc)
            plan_path = argv[++i];
        else if (strcmp(argv[i], "--min-alt") == 0)
        {
            if (parse_double_arg(argc, argv, i, &min_alt_m) != 0)
            {
                flight_test_print_usage(argv[0]);
                return 2;
            }
            ++i;
        }
        else if (strcmp(argv[i], "--min-reserve-fuel") == 0)
        {
            if (parse_double_arg(argc, argv, i, &min_reserve_fuel_kg) != 0)
            {
                flight_test_print_usage(argv[0]);
                return 2;
            }
            ++i;
        }
        else if (strcmp(argv[i], "--step") == 0)
        {
            if (parse_int_arg(argc, argv, i, &step_seconds) != 0)
            {
                flight_test_print_usage(argv[0]);
                return 2;
            }
            ++i;
        }
        else if (strcmp(argv[i], "--output") == 0 && i + 1 < argc)
            output_path = argv[++i];
        else if (strcmp(argv[i], "--help") == 0 || strcmp(argv[i], "-h") == 0)
        {
            flight_test_print_usage(argv[0]);
            return 0;
        }
        else
        {
            fprintf(stderr, "Unknown argument: %s\n", argv[i]);
            flight_test_print_usage(argv[0]);
            return 2;
        }
    }

    if (!plan_path)
    {
        flight_test_print_usage(argv[0]);
        return 2;
    }

    FlightTestResult result;
    char err[256];
    if (run_flight_test(plan_path, min_alt_m, min_reserve_fuel_kg, step_seconds, &result, err, sizeof(err)) != 0)
    {
        fprintf(stderr, "Flight test error: %s\n", err);
        return 2;
    }

    printf("%s\n", result.summary);
    if (result.altitude_violations > 0)
        printf("  - Altitude below minimum (%.1f m) detected %d time(s)\n", min_alt_m, result.altitude_violations);
    if (result.fuel_violation)
        printf("  - Remaining fuel (%.1f kg) is below required reserve (%.1f kg)\n",
               result.fuel_remaining_kg, min_reserve_fuel_kg);

    if (output_path)
    {
        FILE *report = fopen(output_path, "w");
        if (!report)
        {
            fprintf(stderr, "Cannot write report to '%s'\n", output_path);
            return 2;
        }
        fprintf(report, "Flight Plan Test Report\n");
        fprintf(report, "=======================\n");
        fprintf(report, "Plan file: %s\n", plan_path);
        fprintf(report, "Result: %s\n", result.passed ? "PASS" : "FAIL");
        fprintf(report, "%s\n", result.summary);
        fprintf(report, "Minimum altitude (m): %.1f\n", min_alt_m);
        fprintf(report, "Minimum reserve fuel (kg): %.1f\n", min_reserve_fuel_kg);
        fprintf(report, "Altitude violations: %d\n", result.altitude_violations);
        fprintf(report, "Fuel violation: %s\n", result.fuel_violation ? "YES" : "NO");
        fclose(report);
        printf("Report written to %s\n", output_path);
    }

    return result.passed ? 0 : 1;
}
