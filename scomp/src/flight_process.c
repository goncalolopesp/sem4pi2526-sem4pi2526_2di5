#include "simulation.h"
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

/* Processo filho que simula um voo. Recebe SIGUSR1 para arrancar e envia updates pelo pipe. */
static volatile sig_atomic_t start_requested = 0;
static volatile sig_atomic_t terminate_requested = 0;

static void handle_start(int signo) {
    (void)signo;
    start_requested = 1;
}

static void handle_term(int signo) {
    (void)signo;
    terminate_requested = 1;
}

// US103: avancar um passo quando o processo pai autoriza
static volatile sig_atomic_t next_step_requested = 0;

static void handle_next_step(int signo) {
    (void)signo;
    next_step_requested = 1;
}

/* Handlers mínimos: inicio e termo. Uso sig_atomic_t para segurança em sinais. */
static Position interpolate(Position a, Position b, double ratio) {
    Position p;
    p.latitude = a.latitude + (b.latitude - a.latitude) * ratio;
    p.longitude = a.longitude + (b.longitude - a.longitude) * ratio;
    p.altitude_m = a.altitude_m + (b.altitude_m - a.altitude_m) * ratio;
    return p;
}

/* Interpolação linear entre duas posições. */
static int send_update(int fd, const FlightPlan *plan, int t, Position p, int status) {
    PositionUpdate update;
    memset(&update, 0, sizeof(update));
    update.time_step = t;
    update.pid = getpid();
    update.position = p;
    update.status = status;
    snprintf(update.flight_id, sizeof(update.flight_id), "%s", plan->flight_id);
    ssize_t written = write(fd, &update, sizeof(update));
    return written == (ssize_t)sizeof(update) ? 0 : -1;
}

/* Envia um PositionUpdate pelo pipe. Uso formato fixo para simplificar leitura pelo processo pai. */
void run_flight_process(const char *flight_file, int write_fd, const SimulationConfig *config) {
    struct sigaction start_action;
    struct sigaction term_action;
    memset(&start_action, 0, sizeof(start_action));
    memset(&term_action, 0, sizeof(term_action));
    start_action.sa_handler = handle_start;
    term_action.sa_handler = handle_term;
    sigemptyset(&start_action.sa_mask);
    sigemptyset(&term_action.sa_mask);
    start_action.sa_flags = SA_RESTART;
    term_action.sa_flags = SA_RESTART;
    sigaction(SIGUSR1, &start_action, NULL);
    sigaction(SIGTERM, &term_action, NULL);
    sigaction(SIGINT, &term_action, NULL);

    // US103: sinal de avanco de passo
    struct sigaction next_action;
    memset(&next_action, 0, sizeof(next_action));
    next_action.sa_handler = handle_next_step;
    sigemptyset(&next_action.sa_mask);
    next_action.sa_flags = SA_RESTART;
    sigaction(SIGUSR2, &next_action, NULL);

    sigset_t startup_mask;
    sigemptyset(&startup_mask);
    sigaddset(&startup_mask, SIGUSR1);
    sigprocmask(SIG_UNBLOCK, &startup_mask, NULL);

    FlightPlan plan;
    char err[256];
    if (parse_flight_plan(flight_file, &plan, err, sizeof(err)) != 0) {
        dprintf(STDERR_FILENO, "Flight process error: %s\n", err);
        close(write_fd);
        _exit(EXIT_FAILURE);
    }

    while (!start_requested && !terminate_requested) pause();
    if (terminate_requested) {
        close(write_fd);
        _exit(EXIT_SUCCESS);
    }

    /* Espero pelo sinal de arranque; isto permite sincronizar todos os filhos. */
    int total_steps = (config->end_time - config->start_time) / config->step_seconds;
    if (total_steps < plan.segment_count) total_steps = plan.segment_count;
    int steps_per_segment = total_steps / plan.segment_count;
    if (steps_per_segment <= 0) steps_per_segment = 1;

    int current_time = config->start_time;
    for (int i = 0; i < plan.segment_count && !terminate_requested; ++i) {
        Segment *seg = &plan.segments[i];
        for (int s = 0; s < steps_per_segment && current_time <= config->end_time && !terminate_requested; ++s) {
            double ratio = steps_per_segment == 1 ? 1.0 : (double)s / (double)(steps_per_segment - 1);
            Position p = interpolate(seg->start, seg->end, ratio);

            next_step_requested = 0;
            send_update(write_fd, &plan, current_time, p, STATUS_RUNNING);

            // US103: aguarda autorizacao do processo pai
            sigset_t empty_mask;
            sigemptyset(&empty_mask);
            while (!next_step_requested && !terminate_requested) {
                sigsuspend(&empty_mask);
            }

            current_time += config->step_seconds;
//            usleep(50000);
        }
    }

    Position final_position = plan.segments[plan.segment_count - 1].end;
    send_update(write_fd, &plan, current_time, final_position,
                terminate_requested ? STATUS_ABORTED : STATUS_FINISHED);
    close(write_fd);
    _exit(EXIT_SUCCESS);
}
