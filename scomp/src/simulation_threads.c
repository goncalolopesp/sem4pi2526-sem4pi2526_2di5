#define _POSIX_C_SOURCE 200809L

#include "simulation.h"

#include <stdio.h>
#include <string.h>

void simulation_threads_init(SimulationThreadContext *ctx,
                             SharedSimulationState *shared,
                             const SimulationConfig *config,
                             SafetyLog *safety_log,
                             FlightRuntime *runtimes,
                             int runtime_count)
{
    memset(ctx, 0, sizeof(*ctx));

    ctx->shared = shared;
    ctx->config = config;
    ctx->safety_log = safety_log;
    ctx->runtimes = runtimes;
    ctx->runtime_count = runtime_count;
    ctx->stop = 0;
    ctx->violations_reported = 0;

    snprintf(ctx->realtime_log_path, MAX_PATH, "%s", config->output_file);
    size_t len = strlen(ctx->realtime_log_path);
    if (len >= 4 && strcmp(ctx->realtime_log_path + len - 4, ".txt") == 0)
    {
        snprintf(ctx->realtime_log_path + len - 4, MAX_PATH - (len - 4),
                 "_violations.log");
    }
    else
    {
        snprintf(ctx->realtime_log_path, MAX_PATH, "simulation_violations.log");
    }

    pthread_mutex_init(&ctx->report_mutex, NULL);
    pthread_cond_init(&ctx->violation_cond, NULL);
}

void simulation_threads_destroy(SimulationThreadContext *ctx)
{
    pthread_mutex_destroy(&ctx->report_mutex);
    pthread_cond_destroy(&ctx->violation_cond);
}

void *safety_detection_thread(void *arg)
{
    SimulationThreadContext *ctx = arg;
    int last_seen = 0;

    while (!ctx->stop)
    {
        pthread_mutex_lock(&ctx->shared->pthread_mutex);

        while (ctx->shared->shared_update_counter == last_seen && !ctx->stop)
        {
            pthread_cond_wait(&ctx->shared->pthread_cond, &ctx->shared->pthread_mutex);
        }

        last_seen = ctx->shared->shared_update_counter;

        pthread_mutex_unlock(&ctx->shared->pthread_mutex);

        if (ctx->stop)
            break;

        pthread_mutex_lock(&ctx->report_mutex);

        int new_v = safety_check_shared(ctx->shared,
                                        ctx->config,
                                        ctx->safety_log,
                                        ctx->runtimes,
                                        ctx->runtime_count);

        if (new_v > 0)
        {
            fprintf(stderr,
                    "[US107] safety thread: %d new violation(s), total=%d\n",
                    new_v, ctx->safety_log->total_detected);
            pthread_cond_signal(&ctx->violation_cond);
        }

        pthread_mutex_unlock(&ctx->report_mutex);
    }

    return NULL;
}

void *report_generation_thread(void *arg)
{
    SimulationThreadContext *ctx = arg;
    FILE *realtime_log = fopen(ctx->realtime_log_path, "w");

    if (realtime_log)
    {
        fprintf(realtime_log, "US107 Real-time Safety Violation Log\n");
        fprintf(realtime_log, "===================================\n");
        fflush(realtime_log);
    }

    while (!ctx->stop)
    {
        pthread_mutex_lock(&ctx->report_mutex);

        while (ctx->safety_log->count == ctx->violations_reported && !ctx->stop)
        {
            pthread_cond_wait(&ctx->violation_cond, &ctx->report_mutex);
        }

        while (ctx->violations_reported < ctx->safety_log->count)
        {
            const SafetyViolation *v =
                &ctx->safety_log->events[ctx->violations_reported];

            if (v->type == VIOLATION_ALTITUDE)
            {
                fprintf(stderr,
                        "[US107 report] t=%d ALTITUDE flight=%s pid=%d alt=%.1fm\n",
                        v->time_step, v->flight_a, v->pid_a, v->altitude_m);
            }
            else
            {
                fprintf(stderr,
                        "[US107 report] t=%d SEPARATION %s(pid=%d) <-> %s(pid=%d) dist=%.2f NM\n",
                        v->time_step,
                        v->flight_a, v->pid_a,
                        v->flight_b, v->pid_b,
                        v->distance_nm);
            }

            safety_log_event_realtime(realtime_log, v, ctx->config);
            ctx->violations_reported++;
        }

        pthread_mutex_unlock(&ctx->report_mutex);
    }

    pthread_mutex_lock(&ctx->report_mutex);

    while (ctx->violations_reported < ctx->safety_log->count)
    {
        const SafetyViolation *v =
            &ctx->safety_log->events[ctx->violations_reported];
        safety_log_event_realtime(realtime_log, v, ctx->config);
        ctx->violations_reported++;
    }

    pthread_mutex_unlock(&ctx->report_mutex);

    if (realtime_log)
        fclose(realtime_log);

    return NULL;
}
