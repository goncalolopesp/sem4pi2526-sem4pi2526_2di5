#define _POSIX_C_SOURCE 200809L

#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/mman.h>
#include <sys/wait.h>
#include <unistd.h>
#include <semaphore.h>

#include "../src/simulation.h"

int main(void)
{
    shm_unlink(SHM_NAME);

    SharedSimulationState *state = shared_create();

    assert(state != NULL);

    pid_t pid = fork();

    if (pid < 0)
    {
        perror("fork");
        shared_destroy(state);
        return EXIT_FAILURE;
    }

    if (pid == 0)
    {
        /* Filho simula o comportamento da US108:
           fica bloqueado até o pai libertar o timestep. */
        sem_wait(&state->step_start[0]);

        sem_wait(&state->mutex);
        state->flights[0].pid = getpid();
        state->flights[0].time_step = 30;
        state->flights[0].status = STATUS_RUNNING;
        state->shared_update_counter++;
        sem_post(&state->mutex);

        _exit(EXIT_SUCCESS);
    }

    sleep(1);

    /* Antes do sem_post do pai, o filho não pode ter avançado. */
    sem_wait(&state->mutex);
    assert(state->shared_update_counter == 0);
    sem_post(&state->mutex);

    /* Pai autoriza o próximo timestep. */
    sem_post(&state->step_start[0]);

    int status;
    waitpid(pid, &status, 0);

    assert(WIFEXITED(status));
    assert(WEXITSTATUS(status) == EXIT_SUCCESS);

    /* Depois do sem_post, o filho avançou. */
    sem_wait(&state->mutex);
    assert(state->shared_update_counter == 1);
    assert(state->flights[0].time_step == 30);
    assert(state->flights[0].status == STATUS_RUNNING);
    sem_post(&state->mutex);

    shared_destroy(state);

    printf("test_step_synchronization OK\n");

    return EXIT_SUCCESS;
}