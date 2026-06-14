#define _POSIX_C_SOURCE 200809L

#include "simulation.h"

#include <fcntl.h>
#include <sys/mman.h>
#include <unistd.h>
#include <string.h>

SharedSimulationState *shared_create(void)
{
    int fd =
        shm_open(
            SHM_NAME,
            O_CREAT | O_RDWR,
            0666);

    ftruncate(
        fd,
        sizeof(SharedSimulationState));

    SharedSimulationState *state =
        mmap(
            NULL,
            sizeof(SharedSimulationState),
            PROT_READ | PROT_WRITE,
            MAP_SHARED,
            fd,
            0);

    memset(
        state,
        0,
        sizeof(*state));

    sem_init(
        &state->mutex,
        1,
        1);

    sem_init(
        &state->update_sem,
        1,
        0);

/* Semáforos usados para sincronização timestep a timestep
   entre o processo pai e cada processo filho (US108). */
    for (int i = 0; i < MAX_FLIGHTS; ++i)
    {
        sem_init(
            &state->step_start[i],
            1,
            0);
    }

/* Flag global para terminar todos os processos filho
   quando a simulação termina ou ocorre falha. */
    state->terminate = 0;

    pthread_mutexattr_t mutex_attr;
    pthread_condattr_t cond_attr;

    pthread_mutexattr_init(&mutex_attr);
    pthread_mutexattr_setpshared(&mutex_attr, PTHREAD_PROCESS_SHARED);

    pthread_condattr_init(&cond_attr);
    pthread_condattr_setpshared(&cond_attr, PTHREAD_PROCESS_SHARED);

    pthread_mutex_init(&state->pthread_mutex, &mutex_attr);
    pthread_cond_init(&state->pthread_cond, &cond_attr);

    pthread_mutexattr_destroy(&mutex_attr);
    pthread_condattr_destroy(&cond_attr);

    state->shared_update_counter = 0;

    close(fd);

    return state;
}

void shared_destroy(SharedSimulationState *state)
{
    sem_destroy(&state->mutex);

    sem_destroy(&state->update_sem);

    for (int i = 0; i < MAX_FLIGHTS; ++i)
    {
        sem_destroy(&state->step_start[i]);
    }

    pthread_mutex_destroy(&state->pthread_mutex);
    pthread_cond_destroy(&state->pthread_cond);

    munmap(
        state,
        sizeof(*state));

    shm_unlink(
        SHM_NAME);
}