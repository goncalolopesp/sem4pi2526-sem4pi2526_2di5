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

    close(fd);

    return state;
}

void shared_destroy(SharedSimulationState *state)
{
    sem_destroy(&state->mutex);

    sem_destroy(&state->update_sem);

    munmap(
        state,
        sizeof(*state));

    shm_unlink(
        SHM_NAME);
}