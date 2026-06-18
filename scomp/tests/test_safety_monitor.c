#include <assert.h>
#include <stdio.h>
#include <string.h>

#include "../src/simulation.h"

/* Altitude inferior ao limite configurado.
 * Deve originar uma violação do tipo VIOLATION_ALTITUDE.
 */
int main(void)
{

    PositionTracker tracker;
    tracker_init(&tracker);

    SimulationConfig config;

    memset(&config, 0, sizeof(config));

    config.safety_min_sep_nm = 3.0;
    config.safety_min_alt_m = 300;

    /* Cria um estado de voo que viola de propósito o limite mínimo de altitude. */
    PositionUpdate update;

    memset(&update, 0, sizeof(update));

    strcpy(update.flight_id, "LOW-ALT");

    update.pid = 1;

    update.time_step = 0;

    update.position.latitude = 41;
    update.position.longitude = -8;
    update.position.altitude_m = 100;

    update.status = STATUS_RUNNING;

    /* Coloca no tracker a posição que o monitor de segurança vai verificar. */
    tracker_update(&tracker, &update);

    SafetyLog log;

    safety_log_init(&log);

    FlightRuntime runtimes[1];

    memset(runtimes, 0, sizeof(runtimes));

    strcpy(runtimes[0].flight_id, "LOW-ALT");

    runtimes[0].pid = 1;
    runtimes[0].active = 1;

    /* Executa a lógica de deteção de violações.
     * Equivale ao que acontece na thread de segurança durante a simulação.
     */
    int violations =
        safety_check_step(
            &tracker,
            &config,
            0,
            &log,
            runtimes,
            1);

    /* Garante que o voo abaixo do limite é contado como violação. */
    assert(violations > 0);

    /* Confirma que a violação fica registada no log para o relatório. */
    assert(log.count == 1);

    tracker_destroy(&tracker);

    printf("test_safety_monitor OK\n");

    return 0;
}
