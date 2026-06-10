#define _POSIX_C_SOURCE 200809L

#include <assert.h>
#include <pthread.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include "../src/simulation.h"

typedef struct
{
    pthread_mutex_t mutex;
    pthread_cond_t cond;
    int pending;
    volatile sig_atomic_t stop;
} NotifyTestContext;

static void *producer_thread(void *arg)
{
    NotifyTestContext *ctx = arg;

    usleep(50000);

    pthread_mutex_lock(&ctx->mutex);
    ctx->pending = 1;
    pthread_cond_signal(&ctx->cond);
    pthread_mutex_unlock(&ctx->mutex);

    return NULL;
}

static void *consumer_thread(void *arg)
{
    NotifyTestContext *ctx = arg;

    pthread_mutex_lock(&ctx->mutex);

    while (ctx->pending == 0 && !ctx->stop)
    {
        pthread_cond_wait(&ctx->cond, &ctx->mutex);
    }

    assert(ctx->pending == 1);

    pthread_mutex_unlock(&ctx->mutex);

    return NULL;
}

int main(void)
{
    NotifyTestContext ctx;
    memset(&ctx, 0, sizeof(ctx));

    pthread_mutex_init(&ctx.mutex, NULL);
    pthread_cond_init(&ctx.cond, NULL);

    pthread_t producer;
    pthread_t consumer;

    pthread_create(&consumer, NULL, consumer_thread, &ctx);
    pthread_create(&producer, NULL, producer_thread, &ctx);

    pthread_join(producer, NULL);
    pthread_join(consumer, NULL);

    pthread_mutex_destroy(&ctx.mutex);
    pthread_cond_destroy(&ctx.cond);

    printf("test_simulation_threads OK\n");

    return 0;
}
