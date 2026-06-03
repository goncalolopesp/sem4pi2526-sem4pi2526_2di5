#define _POSIX_C_SOURCE 200809L

#include <assert.h>
#include <pthread.h>
#include <stdio.h>
#include <sys/mman.h>

#include "../src/simulation.h"

static void *wait_for_shared_update(void *arg)
{
    SharedSimulationState *state = arg;

    pthread_mutex_lock(&state->pthread_mutex);

    while (state->shared_update_counter == 0)
    {
        pthread_cond_wait(&state->pthread_cond, &state->pthread_mutex);
    }

    pthread_mutex_unlock(&state->pthread_mutex);

    return NULL;
}

int main(void)
{
    shm_unlink(SHM_NAME);

    SharedSimulationState *state = shared_create();

    assert(state != NULL);
    assert(state->active_flights == 0);
    assert(state->shared_update_counter == 0);

    sem_wait(&state->mutex);

    state->active_flights = 1;
    state->flights[0].pid = 1234;
    state->flights[0].position.latitude = 41.0;
    state->flights[0].position.longitude = -8.0;
    state->flights[0].position.altitude_m = 1000.0;
    state->flights[0].time_step = 10;
    state->flights[0].status = STATUS_RUNNING;

    sem_post(&state->mutex);

    pthread_t thread;
    pthread_create(&thread, NULL, wait_for_shared_update, state);

    pthread_mutex_lock(&state->pthread_mutex);

    state->shared_update_counter++;

    pthread_cond_signal(&state->pthread_cond);

    pthread_mutex_unlock(&state->pthread_mutex);

    pthread_join(thread, NULL);

    assert(state->active_flights == 1);
    assert(state->shared_update_counter == 1);
    assert(state->flights[0].pid == 1234);
    assert(state->flights[0].time_step == 10);
    assert(state->flights[0].status == STATUS_RUNNING);

    shared_destroy(state);

    printf("test_shared_simulation OK\n");

    return 0;
}