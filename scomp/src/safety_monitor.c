#define _POSIX_C_SOURCE 200809L
#include "simulation.h"
#include <math.h>
#include <signal.h>
#include <stdio.h>
#include <string.h>

#define EARTH_RADIUS_NM 3440.065   /* raio da Terra em milhas náuticas */

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

void safety_log_init(SafetyLog *log) {
    memset(log, 0, sizeof(*log));
}

/* Distância haversine entre duas posições, em milhas náuticas */
static double haversine_nm(Position a, Position b) {
    double lat1 = a.latitude  * M_PI / 180.0;
    double lat2 = b.latitude  * M_PI / 180.0;
    double dlat = (b.latitude  - a.latitude)  * M_PI / 180.0;
    double dlon = (b.longitude - a.longitude) * M_PI / 180.0;

    double h = sin(dlat/2) * sin(dlat/2) +
               cos(lat1) * cos(lat2) * sin(dlon/2) * sin(dlon/2);
    double c = 2 * atan2(sqrt(h), sqrt(1 - h));
    return EARTH_RADIUS_NM * c;
}

/*
static double equirectangular_nm(Position a, Position b) {
    double avg_lat = (a.latitude + b.latitude) / 2.0 * (M_PI / 180.0);

    double dlat = (b.latitude - a.latitude) * 60.0;
    double dlon = (b.longitude - a.longitude) * 60.0 * cos(avg_lat);

    return sqrt(dlat * dlat + dlon * dlon);
}
*/

/* Procura o PID associado a um flight_id no array de runtimes. */
static pid_t pid_for_flight(const char *flight_id,
                            const FlightRuntime *runtimes, int n) {
    for (int i = 0; i < n; ++i) {
        if (strcmp(runtimes[i].flight_id, flight_id) == 0)
            return runtimes[i].pid;
    }
    return 0;
}

/* Regista uma violação no log (se houver espaço) e devolve 1 se foi nova. */
static int log_violation(SafetyLog *log, const SafetyViolation *v) {
    log->total_detected++;
    if (log->count < MAX_VIOLATIONS) {
        log->events[log->count++] = *v;
        return 1;
    }
    return 0;
}

int safety_check_step(const PositionTracker *tracker,
                      const SimulationConfig *config,
                      int current_step,
                      SafetyLog *log,
                      const FlightRuntime *runtimes,
                      int runtime_count) {
    int new_violations = 0;

    /* Só comparamos voos que estejam em RUNNING e cujo último update
       corresponda ao passo atual. */
    for (int i = 0; i < tracker->entry_count; ++i) {
        const TrackEntry *a = &tracker->entries[i];
        if (a->status != STATUS_RUNNING) continue;
        if (a->last_time_step != current_step) continue;

        /* (1) Violação de altitude mínima */
        if (a->current_position.altitude_m < config->safety_min_alt_m) {
            SafetyViolation v;
            memset(&v, 0, sizeof(v));
            v.time_step  = current_step;
            v.type       = VIOLATION_ALTITUDE;
            snprintf(v.flight_a, sizeof(v.flight_a), "%s", a->flight_id);
            v.pid_a      = pid_for_flight(a->flight_id, runtimes, runtime_count);
            v.altitude_m = a->current_position.altitude_m;

            if (log_violation(log, &v)) new_violations++;
            if (v.pid_a > 0) kill(v.pid_a, SIGUSR1);
        }

        /* (2) Violação de separação horizontal entre pares */
        for (int j = i + 1; j < tracker->entry_count; ++j) {
            const TrackEntry *b = &tracker->entries[j];
            if (b->status != STATUS_RUNNING) continue;
            if (b->last_time_step != current_step) continue;

            /* TRIAGEM 1: Altitude (É o cálculo mais leve de todos) */
            double dalt = fabs(a->current_position.altitude_m - b->current_position.altitude_m);
            if (dalt >= config->safety_min_alt_m) {
                continue; /* Estão separados em altura, ignorar o resto! */
            }

            /* TRIAGEM 2: Latitude Bruta (1 grau = 60 milhas náuticas) */
            double dlat_bruta = fabs(a->current_position.latitude - b->current_position.latitude) * 60.0;
            if (dlat_bruta >= config->safety_min_sep_nm) {
                continue; /* Estão demasiado longe a Norte/Sul, ignorar! */
            }

            /* MATEMÁTICA FINA: Só chega aqui se estiverem na mesma zona e mesma altitude */
            double dist = haversine_nm(a->current_position, b->current_position);

            /* Confirmação final exata */
            if (dist < config->safety_min_sep_nm) {
                SafetyViolation v;
                memset(&v, 0, sizeof(v));
                v.time_step   = current_step;
                v.type        = VIOLATION_SEPARATION;
                snprintf(v.flight_a, sizeof(v.flight_a), "%s", a->flight_id);
                snprintf(v.flight_b, sizeof(v.flight_b), "%s", b->flight_id);
                v.pid_a       = pid_for_flight(a->flight_id, runtimes, runtime_count);
                v.pid_b       = pid_for_flight(b->flight_id, runtimes, runtime_count);
                v.distance_nm = dist;

                if (log_violation(log, &v)) new_violations++;
                if (v.pid_a > 0) kill(v.pid_a, SIGUSR1);
                if (v.pid_b > 0) kill(v.pid_b, SIGUSR1);
            }
        }
    }

    return new_violations;
}

void safety_log_print(const SafetyLog *log) {
    printf("\n=== US102 Safety Violations (%d shown / %d total) ===\n",
           log->count, log->total_detected);
    for (int i = 0; i < log->count; ++i) {
        const SafetyViolation *v = &log->events[i];
        if (v->type == VIOLATION_ALTITUDE) {
            printf("  t=%d ALTITUDE  flight=%s pid=%d alt=%.1fm\n",
                   v->time_step, v->flight_a, v->pid_a, v->altitude_m);
        } else {
            printf("  t=%d SEPARATION %s(pid=%d) <-> %s(pid=%d) dist=%.2f NM\n",
                   v->time_step,
                   v->flight_a, v->pid_a,
                   v->flight_b, v->pid_b,
                   v->distance_nm);
        }
    }
    printf("=====================================================\n\n");
}