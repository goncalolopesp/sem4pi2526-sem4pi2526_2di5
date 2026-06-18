#define _POSIX_C_SOURCE 200809L

#include <assert.h>
#include <pthread.h>
#include <stdio.h>
#include <sys/mman.h>

#include "../src/simulation.h"

static void *wait_for_shared_update(void *arg)
{
    SharedSimulationState *state = arg;

    /* Protege a condição enquanto a thread observa o estado partilhado. */
    pthread_mutex_lock(&state->pthread_mutex);

    while (state->shared_update_counter == 0)
    {
        /* Simula a thread consumidora à espera de uma notificação de atualização. */
        pthread_cond_wait(&state->pthread_cond, &state->pthread_mutex);
    }

    pthread_mutex_unlock(&state->pthread_mutex);

    return NULL;
}

int main(void)
{
    /* Valida a criação da memória partilhada usada pelos processos filho.
     * A memória é criada com shm_open + mmap.
     */
    shm_unlink(SHM_NAME);

    SharedSimulationState *state = shared_create();

    assert(state != NULL);
    assert(state->active_flights == 0);
    assert(state->shared_update_counter == 0);

    /* Protege a escrita na memória partilhada.
     * Evita condições de corrida quando vários processos acedem aos mesmos dados.
     */
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

    /* Publica uma atualização e acorda a thread bloqueada na condição. */
    pthread_mutex_lock(&state->pthread_mutex);

    state->shared_update_counter++;

    /* Confirma o mecanismo de notificação usado para coordenar atualizações. */
    pthread_cond_signal(&state->pthread_cond);

    pthread_mutex_unlock(&state->pthread_mutex);

    pthread_join(thread, NULL);

    /* Verifica que os dados escritos continuam visíveis depois da sincronização. */
    assert(state->active_flights == 1);
    assert(state->shared_update_counter == 1);
    assert(state->flights[0].pid == 1234);
    assert(state->flights[0].time_step == 10);
    assert(state->flights[0].status == STATUS_RUNNING);

    shared_destroy(state);

    printf("test_shared_simulation OK\n");

    return 0;
}
