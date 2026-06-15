#define _POSIX_C_SOURCE 200809L

#include <assert.h>
#include <pthread.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include "../src/simulation.h"

/* Simulates the main process loop unlocking a new simulation step */
static void *producer_thread(void *arg)
{
    SimulationThreadContext *ctx = arg;

    usleep(50000);

    pthread_mutex_lock(&ctx->step_mutex);
    ctx->steps_ready++;
    pthread_cond_signal(&ctx->step_cond);
    pthread_mutex_unlock(&ctx->step_mutex);

    return NULL;
}

/* Simulates the safety monitor thread waiting for the step signal */
static void *consumer_thread(void *arg)
{
    SimulationThreadContext *ctx = arg;

    pthread_mutex_lock(&ctx->step_mutex);

    while (ctx->steps_ready == ctx->steps_checked && !ctx->stop)
    {
        pthread_cond_wait(&ctx->step_cond, &ctx->step_mutex);
    }

    assert(ctx->steps_ready == 1);
    ctx->steps_checked = ctx->steps_ready;

    pthread_mutex_unlock(&ctx->step_mutex);

    return NULL;
}

int main(void)
{
    // DEFESA:
    // Este teste isola o protocolo condition-variable entre produtor de passos e consumidor de segurança.
    SimulationThreadContext ctx;
    SharedSimulationState shared;
    SimulationConfig config;
    SafetyLog log;

    /* Initialize dummy structures required to setup the thread context */
    memset(&shared, 0, sizeof(shared));
    memset(&config, 0, sizeof(config));
    memset(&log, 0, sizeof(log));
    strcpy(config.output_file, "dummy_test_report.txt");

    /* Test the actual US106 initialization function */
    simulation_threads_init(&ctx, &shared, &config, &log, NULL, 0);

    assert(ctx.steps_ready == 0);
    assert(ctx.steps_checked == 0);

    pthread_t producer;
    pthread_t consumer;

    pthread_create(&consumer, NULL, consumer_thread, &ctx);
    pthread_create(&producer, NULL, producer_thread, &ctx);

    pthread_join(producer, NULL);
    pthread_join(consumer, NULL);

    /* Test the cleanup function */
    simulation_threads_destroy(&ctx);

    printf("test_simulation_threads OK\n");

    return 0;
}
