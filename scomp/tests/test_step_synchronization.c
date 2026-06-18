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
        /* O filho fica bloqueado neste semáforo.
         * Isto prova que um voo não avança sozinho para o próximo passo.
         * Apenas o processo pai pode libertar o voo através de sem_post().
         */
        sem_wait(&state->step_start[0]);

        /* Atualiza o estado partilhado apenas dentro da secção protegida. */
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

    /* O pai autoriza explicitamente o próximo passo.
     * Este teste valida a sincronização passo-a-passo da US108.
     */
    sem_post(&state->step_start[0]);

    int status;
    waitpid(pid, &status, 0);

    assert(WIFEXITED(status));
    assert(WEXITSTATUS(status) == EXIT_SUCCESS);

    /* Depois do sem_post, o filho avançou. */
    /* Depois da autorização, o filho tem de publicar o passo na memória partilhada. */
    sem_wait(&state->mutex);
    assert(state->shared_update_counter == 1);
    assert(state->flights[0].time_step == 30);
    assert(state->flights[0].status == STATUS_RUNNING);
    sem_post(&state->mutex);

    shared_destroy(state);

    printf("test_step_synchronization OK\n");

    return EXIT_SUCCESS;
}
