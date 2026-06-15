#define _POSIX_C_SOURCE 200809L
#include "simulation.h"
#include <math.h>
#include <signal.h>
#include <stdio.h>
#include <string.h>

#define EARTH_RADIUS_NM 3440.065 /* raio da Terra em milhas náuticas */

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

void safety_log_init(SafetyLog *log)
{
    memset(log, 0, sizeof(*log));
}

/* Distância haversine entre duas posições, em milhas náuticas */
static double haversine_nm(Position a, Position b)
{
    double lat1 = a.latitude * M_PI / 180.0;
    double lat2 = b.latitude * M_PI / 180.0;
    double dlat = (b.latitude - a.latitude) * M_PI / 180.0;
    double dlon = (b.longitude - a.longitude) * M_PI / 180.0;

    double h = sin(dlat / 2) * sin(dlat / 2) +
               cos(lat1) * cos(lat2) * sin(dlon / 2) * sin(dlon / 2);
    double c = 2 * atan2(sqrt(h), sqrt(1 - h));
    return EARTH_RADIUS_NM * c;
}

/* Procura o PID associado a um flight_id no array de runtimes. */
static pid_t pid_for_flight(const char *flight_id,
                            const FlightRuntime *runtimes, int n)
{
    for (int i = 0; i < n; ++i)
    {
        if (strcmp(runtimes[i].flight_id, flight_id) == 0)
            return runtimes[i].pid;
    }
    return 0;
}

/* Regista uma violação no log (se houver espaço) e devolve 1 se foi nova. */
static int log_violation(SafetyLog *log, const SafetyViolation *v)
{
    log->total_detected++;
    if (log->count < MAX_VIOLATIONS)
    {
        log->events[log->count++] = *v;
        return 1;
    }
    return 0;
}

static int violation_already_logged(const SafetyLog *log, const SafetyViolation *candidate)
{
    for (int i = 0; i < log->count; ++i)
    {
        const SafetyViolation *v = &log->events[i];

        if (v->time_step != candidate->time_step || v->type != candidate->type)
            continue;

        if (candidate->type == VIOLATION_ALTITUDE)
        {
            if (strcmp(v->flight_a, candidate->flight_a) == 0)
                return 1;
        }
        else if ((strcmp(v->flight_a, candidate->flight_a) == 0 &&
                  strcmp(v->flight_b, candidate->flight_b) == 0) ||
                 (strcmp(v->flight_a, candidate->flight_b) == 0 &&
                  strcmp(v->flight_b, candidate->flight_a) == 0))
        {
            return 1;
        }
    }

    return 0;
}

static int register_violation(SafetyLog *log, SafetyViolation *v)
{
    if (violation_already_logged(log, v))
        return 0;

    if (!log_violation(log, v))
        return 0;

    if (v->pid_a > 0)
        kill(v->pid_a, SIGUSR1);
    if (v->pid_b > 0)
        kill(v->pid_b, SIGUSR1);

    return 1;
}

void safety_log_event_realtime(FILE *file, const SafetyViolation *v,
                               const SimulationConfig *config)
{
    if (!file || !v || !config)
        return;

    if (v->type == VIOLATION_ALTITUDE)
    {
        fprintf(file,
                "[US107] t=%d ALTITUDE flight=%s pid=%d alt=%.1fm (min=%.1fm)\n",
                v->time_step, v->flight_a, v->pid_a,
                v->altitude_m, config->safety_min_alt_m);
    }
    else
    {
        fprintf(file,
                "[US107] t=%d SEPARATION %s(pid=%d) <-> %s(pid=%d) dist=%.2f NM (min=%.2f NM)\n",
                v->time_step,
                v->flight_a, v->pid_a,
                v->flight_b, v->pid_b,
                v->distance_nm, config->safety_min_sep_nm);
    }

    fflush(file);
}

int safety_check_step(const PositionTracker *tracker,
                      const SimulationConfig *config,
                      int current_step,
                      SafetyLog *log,
                      const FlightRuntime *runtimes,
                      int runtime_count)
{
    int new_violations = 0;

    /* Só comparamos voos que estejam em RUNNING e cujo último update
       corresponda ao passo atual. */
    for (int i = 0; i < tracker->entry_count; ++i)
    {
        const TrackEntry *a = &tracker->entries[i];
        if (a->status != STATUS_RUNNING)
            continue;
        if (a->last_time_step != current_step)
            continue;

        /* (1) Violação de altitude mínima */
        if (a->current_position.altitude_m < config->safety_min_alt_m)
        {
            SafetyViolation v;
            memset(&v, 0, sizeof(v));
            v.time_step = current_step;
            v.type = VIOLATION_ALTITUDE;
            snprintf(v.flight_a, sizeof(v.flight_a), "%s", a->flight_id);
            v.pid_a = pid_for_flight(a->flight_id, runtimes, runtime_count);
            v.altitude_m = a->current_position.altitude_m;

            if (register_violation(log, &v))
                new_violations++;
        }

        /* (2) Violação de separação horizontal entre pares */
        for (int j = i + 1; j < tracker->entry_count; ++j)
        {
            const TrackEntry *b = &tracker->entries[j];
            if (b->status != STATUS_RUNNING)
                continue;
            if (b->last_time_step != current_step)
                continue;

            double dist = haversine_nm(a->current_position, b->current_position);
            double dalt = fabs(a->current_position.altitude_m -
                               b->current_position.altitude_m);

            /* Demasiado próximos horizontal E verticalmente */
            if (dist < config->safety_min_sep_nm &&
                dalt < config->safety_min_alt_m)
            {

                SafetyViolation v;
                memset(&v, 0, sizeof(v));
                v.time_step = current_step;
                v.type = VIOLATION_SEPARATION;
                snprintf(v.flight_a, sizeof(v.flight_a), "%s", a->flight_id);
                snprintf(v.flight_b, sizeof(v.flight_b), "%s", b->flight_id);
                v.pid_a = pid_for_flight(a->flight_id, runtimes, runtime_count);
                v.pid_b = pid_for_flight(b->flight_id, runtimes, runtime_count);
                v.distance_nm = dist;

                if (register_violation(log, &v))
                    new_violations++;
            }
        }
    }

    return new_violations;
}

int safety_check_shared(SharedSimulationState *shared,
                        const SimulationConfig *config,
                        SafetyLog *log,
                        const FlightRuntime *runtimes,
                        int runtime_count)
{
    SharedFlightState snapshot[MAX_TRACKED_FLIGHTS];
    int flight_count = 0;
    int new_violations = 0;

    sem_wait(&shared->mutex);

    flight_count = shared->active_flights;
    if (flight_count > MAX_TRACKED_FLIGHTS)
        flight_count = MAX_TRACKED_FLIGHTS;

    memcpy(snapshot, shared->flights, (size_t)flight_count * sizeof(SharedFlightState));

    sem_post(&shared->mutex);

    for (int i = 0; i < flight_count; ++i)
    {
        const SharedFlightState *a = &snapshot[i];
        if (a->status != STATUS_RUNNING)
            continue;

        if (a->position.altitude_m < config->safety_min_alt_m)
        {
            SafetyViolation v;
            memset(&v, 0, sizeof(v));
            v.time_step = a->time_step;
            v.type = VIOLATION_ALTITUDE;
            snprintf(v.flight_a, sizeof(v.flight_a), "%s", a->flight_id);
            v.pid_a = pid_for_flight(a->flight_id, runtimes, runtime_count);
            if (v.pid_a == 0)
                v.pid_a = a->pid;
            v.altitude_m = a->position.altitude_m;

            if (register_violation(log, &v))
                new_violations++;
        }

        for (int j = i + 1; j < flight_count; ++j)
        {
            const SharedFlightState *b = &snapshot[j];
            if (b->status != STATUS_RUNNING)
                continue;
            if (a->time_step != b->time_step)
                continue;

            double dist = haversine_nm(a->position, b->position);
            double dalt = fabs(a->position.altitude_m - b->position.altitude_m);

            if (dist < config->safety_min_sep_nm && dalt < config->safety_min_alt_m)
            {
                SafetyViolation v;
                memset(&v, 0, sizeof(v));
                v.time_step = a->time_step;
                v.type = VIOLATION_SEPARATION;
                snprintf(v.flight_a, sizeof(v.flight_a), "%s", a->flight_id);
                snprintf(v.flight_b, sizeof(v.flight_b), "%s", b->flight_id);
                v.pid_a = pid_for_flight(a->flight_id, runtimes, runtime_count);
                v.pid_b = pid_for_flight(b->flight_id, runtimes, runtime_count);
                if (v.pid_a == 0)
                    v.pid_a = a->pid;
                if (v.pid_b == 0)
                    v.pid_b = b->pid;
                v.distance_nm = dist;

                if (register_violation(log, &v))
                    new_violations++;
            }
        }
    }

    return new_violations;
}

void safety_log_print(const SafetyLog *log)
{
    printf("\n=== US102 Safety Violations (%d shown / %d total) ===\n",
           log->count, log->total_detected);
    for (int i = 0; i < log->count; ++i)
    {
        const SafetyViolation *v = &log->events[i];
        if (v->type == VIOLATION_ALTITUDE)
        {
            printf("  t=%d ALTITUDE  flight=%s pid=%d alt=%.1fm\n",
                   v->time_step, v->flight_a, v->pid_a, v->altitude_m);
        }
        else
        {
            printf("  t=%d SEPARATION %s(pid=%d) <-> %s(pid=%d) dist=%.2f NM\n",
                   v->time_step,
                   v->flight_a, v->pid_a,
                   v->flight_b, v->pid_b,
                   v->distance_nm);
        }
    }
    printf("=====================================================\n\n");
}