#include "simulation.h"
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

/* Processo filho que simula um voo.
 * - Recebe SIGCONT para arrancar (US100/US101)
 * - Recebe SIGUSR1 quando ATC deteta violação de segurança (US102)
 * - Recebe SIGTERM/SIGINT para terminar com cleanup
 */
static volatile sig_atomic_t start_requested      = 0;
static volatile sig_atomic_t terminate_requested  = 0;
static volatile sig_atomic_t violation_notified   = 0;

static void handle_start(int signo) {
    (void)signo;
    start_requested = 1;
}

static void handle_term(int signo) {
    (void)signo;
    terminate_requested = 1;
}

/* US102: handler de violação de segurança.
 * Apenas funções async-signal-safe (write). */
static void handle_violation(int signo) {
    (void)signo;
    violation_notified = 1;
    write(STDOUT_FILENO,
          "\n[flight] WARNING: ATC reported a safety violation!\n", 53);
}

static Position interpolate(Position a, Position b, double ratio) {
    Position p;
    p.latitude   = a.latitude   + (b.latitude   - a.latitude)   * ratio;
    p.longitude  = a.longitude  + (b.longitude  - a.longitude)  * ratio;
    p.altitude_m = a.altitude_m + (b.altitude_m - a.altitude_m) * ratio;
    return p;
}

static int send_update(int fd, const FlightPlan *plan, int t, Position p, int status) {
    PositionUpdate update;
    memset(&update, 0, sizeof(update));
    update.time_step = t;
    update.pid       = getpid();
    update.position  = p;
    update.status    = status;
    /* Garante flight_id único por processo: <id_do_plano>-<pid>
       Isto evita que múltiplos processos com o mesmo plano sejam fundidos
       no PositionTracker — crítico para a deteção de violações da US102. */
    snprintf(update.flight_id, sizeof(update.flight_id), "%s-%d",
             plan->flight_id, getpid());
    ssize_t written = write(fd, &update, sizeof(update));
    return written == (ssize_t)sizeof(update) ? 0 : -1;
}

void run_flight_process(const char *flight_file,
                        int update_write_fd,
                        int sync_read_fd,
                        const SimulationConfig *config) {

    /* ---- Handler de arranque (SIGCONT) ---- */
    struct sigaction start_action;
    memset(&start_action, 0, sizeof(start_action));
    start_action.sa_handler = handle_start;
    sigemptyset(&start_action.sa_mask);
    start_action.sa_flags = SA_RESTART;
    sigaction(SIGUSR2, &start_action, NULL);

    /* ---- Handler de terminação (SIGTERM, SIGINT) ---- */
    struct sigaction term_action;
    memset(&term_action, 0, sizeof(term_action));
    term_action.sa_handler = handle_term;
    sigemptyset(&term_action.sa_mask);
    term_action.sa_flags = SA_RESTART;
    sigaction(SIGTERM, &term_action, NULL);
    sigaction(SIGINT,  &term_action, NULL);

    /* ---- US102: handler de violação (SIGUSR1) ----
     * Bloqueia TODOS os outros sinais durante a execução do handler,
     * conforme exigido pela US102 (acceptance criteria). */
    struct sigaction violation_action;
    memset(&violation_action, 0, sizeof(violation_action));
    violation_action.sa_handler = handle_violation;
    sigfillset(&violation_action.sa_mask);             /* bloqueia todos durante o handler */
    sigdelset(&violation_action.sa_mask, SIGKILL);     /* SIGKILL não pode ser bloqueado */
    sigdelset(&violation_action.sa_mask, SIGSTOP);     /* SIGSTOP também não */
    violation_action.sa_flags = SA_RESTART;
    sigaction(SIGUSR1, &violation_action, NULL);

    /* ---- Carregar o plano de voo ---- */
    FlightPlan plan;
    char err[256];
    if (parse_flight_plan(flight_file, &plan, err, sizeof(err)) != 0) {
        dprintf(STDERR_FILENO, "Flight process error: %s\n", err);
        close(update_write_fd);
        _exit(EXIT_FAILURE);
    }

    /* ---- Espera pelo sinal de arranque ---- */
    while (!start_requested && !terminate_requested) pause();
    if (terminate_requested) {
        close(update_write_fd);
        close(sync_read_fd);
        _exit(EXIT_SUCCESS);
    }

    /* ---- Cálculo do número de passos ---- */
    int total_steps = (config->end_time - config->start_time) / config->step_seconds;
    if (total_steps < plan.segment_count) total_steps = plan.segment_count;
    int steps_per_segment = total_steps / plan.segment_count;
    if (steps_per_segment <= 0) steps_per_segment = 1;

    int current_time = config->start_time;

    /* ---- Loop principal da simulação ---- */
    for (int i = 0; i < plan.segment_count && !terminate_requested; ++i) {
        Segment *seg = &plan.segments[i];

        for (int s = 0;
             s < steps_per_segment && current_time <= config->end_time && !terminate_requested;
             ++s) {

            double ratio = (steps_per_segment == 1)
                         ? 1.0
                         : (double)s / (double)(steps_per_segment - 1);
            Position p = interpolate(seg->start, seg->end, ratio);

            send_update(update_write_fd, &plan, current_time, p, STATUS_RUNNING);

            /* US103: aguarda autorização do pai para avançar de passo */
            char command;
            ssize_t n = read(sync_read_fd, &command, sizeof(command));
            if (n <= 0 || command != 'N') {
                terminate_requested = 1;
                break;
            }

            current_time += config->step_seconds;
        }
    }

    Position final_position = plan.segments[plan.segment_count - 1].end;
    send_update(update_write_fd, &plan, current_time, final_position,
                terminate_requested ? STATUS_ABORTED : STATUS_FINISHED);

    close(update_write_fd);
    close(sync_read_fd);
    _exit(EXIT_SUCCESS);
}