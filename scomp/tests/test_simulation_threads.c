#define _POSIX_C_SOURCE 200809L

#include <assert.h>
#include <pthread.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include "../src/simulation.h"

/* Simula o processo principal a libertar um novo passo da simulação. */
static void *producer_thread(void *arg)
{
    SimulationThreadContext *ctx = arg;

    usleep(50000);

    /* Protege os contadores enquanto produtor e consumidor sincronizam o passo. */
    pthread_mutex_lock(&ctx->step_mutex);
    ctx->steps_ready++;

    /* Simula o comportamento do processo pai.
     * Quando existe trabalho disponível, acorda a thread de segurança.
     */
    pthread_cond_signal(...);
    pthread_cond_signal(&ctx->step_cond);
    pthread_mutex_unlock(&ctx->step_mutex);

    return NULL;
}

/* Simula a thread de segurança à espera do sinal de um novo passo. */
static void *consumer_thread(void *arg)
{
    SimulationThreadContext *ctx = arg;

    /* Lê os contadores com o mesmo mutex usado pela thread produtora. */
    pthread_mutex_lock(&ctx->step_mutex);

    /* Simula a thread de segurança à espera que exista um passo pronto.
     * A thread fica bloqueada até receber um sinal.
     */
    while (ctx->steps_ready == ctx->steps_checked && !ctx->stop)
    {
        pthread_cond_wait(&ctx->step_cond, &ctx->step_mutex);
    }

    /* Garante que a consumidora só avança depois de existir um passo pronto. */
    assert(ctx->steps_ready == 1);
    ctx->steps_checked = ctx->steps_ready;

    pthread_mutex_unlock(&ctx->step_mutex);

    return NULL;
}

int main(void)
{
    SimulationThreadContext ctx;
    SharedSimulationState shared;
    SimulationConfig config;
    SafetyLog log;

    /* Inicializa estruturas mínimas para criar o contexto das threads. */
    memset(&shared, 0, sizeof(shared));
    memset(&config, 0, sizeof(config));
    memset(&log, 0, sizeof(log));
    strcpy(config.output_file, "dummy_test_report.txt");

    /* Valida que a coordenação começa sem passos pendentes. */
    simulation_threads_init(&ctx, &shared, &config, &log, NULL, 0);

    assert(ctx.steps_ready == 0);
    assert(ctx.steps_checked == 0);

    pthread_t producer;
    pthread_t consumer;

    pthread_create(&consumer, NULL, consumer_thread, &ctx);
    pthread_create(&producer, NULL, producer_thread, &ctx);

    pthread_join(producer, NULL);
    pthread_join(consumer, NULL);

    /* Confirma que a limpeza liberta os recursos de sincronização criados. */
    simulation_threads_destroy(&ctx);

    printf("test_simulation_threads OK\n");

    return 0;
}
