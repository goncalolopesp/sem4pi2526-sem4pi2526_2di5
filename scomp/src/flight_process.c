#define _POSIX_C_SOURCE 200809L
#include "simulation.h"
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <math.h>

/* Processo filho que simula um voo.
 * - Recebe SIGUSR2 para arrancar (US100/US101)
 * - Recebe SIGUSR1 quando ATC deteta violação de segurança (US102)
 * - Recebe SIGTERM/SIGINT para terminar com cleanup
 */
static volatile sig_atomic_t start_requested = 0;
static volatile sig_atomic_t terminate_requested = 0;
static volatile sig_atomic_t violation_notified = 0;

static void handle_start(int signo)
{
    (void)signo;
    start_requested = 1;
}

static void handle_term(int signo)
{
    (void)signo;
    terminate_requested = 1;
}

/* US102: handler de violação de segurança.
 * Apenas funções async-signal-safe (write). */
static void handle_violation(int signo)
{
    (void)signo;
    violation_notified = 1;
    write(STDOUT_FILENO,
          "\n[flight] WARNING: ATC reported a safety violation!\n", 53);
}

static Position interpolate(Position a, Position b, double ratio)
{
    Position p;
    p.latitude = a.latitude + (b.latitude - a.latitude) * ratio;
    p.longitude = a.longitude + (b.longitude - a.longitude) * ratio;
    p.altitude_m = a.altitude_m + (b.altitude_m - a.altitude_m) * ratio;
    return p;
}

static double weather_factor(int weather_code)
{
    switch (weather_code)
    {
    case WEATHER_RAIN:
        return 0.95;
    case WEATHER_FOG:
        return 0.90;
    case WEATHER_STORM:
        return 0.80;
    case WEATHER_CLEAR:
    default:
        return 1.0;
    }
}

static double selected_speed_knots(const FlightPlan *plan, const Segment *seg)
{
    if (strcmp(seg->mode, "climb") == 0)
        return plan->climb_speed_knots;

    if (strcmp(seg->mode, "descend") == 0)
        return plan->descend_speed_knots;

    return plan->cruise_speed_knots;
}

static double selected_vertical_rate_ms(const FlightPlan *plan, const Segment *seg)
{
    if (strcmp(seg->mode, "climb") == 0)
        return plan->climb_rate_ms;

    if (strcmp(seg->mode, "descend") == 0)
        return plan->descend_rate_ms;

    return 0.0;
}

static double distance_nm(Position a, Position b)
{
    double avg_lat_rad = ((a.latitude + b.latitude) / 2.0) * 3.141592653589793 / 180.0;

    double dlat_nm = (b.latitude - a.latitude) * 60.0;
    double dlon_nm = (b.longitude - a.longitude) * 60.0 * cos(avg_lat_rad);

    return sqrt(dlat_nm * dlat_nm + dlon_nm * dlon_nm);
}

static int send_update(int fd, const FlightPlan *plan, int t, Position p, int status)
{
    PositionUpdate update;
    memset(&update, 0, sizeof(update));
    update.time_step = t;
    update.pid = getpid();
    update.position = p;
    update.status = status;
    /* Garante flight_id único por processo: <id_do_plano>-<pid>
       Isto evita que múltiplos processos com o mesmo plano sejam fundidos
       no PositionTracker — crítico para a deteção de violações da US102. */
    snprintf(update.flight_id, sizeof(update.flight_id), "%.48s-%d",
             plan->flight_id, getpid());
    ssize_t written = write(fd, &update, sizeof(update));
    return written == (ssize_t)sizeof(update) ? 0 : -1;
}

void run_flight_process(const char *flight_file,
                        int update_write_fd,
                        int sync_read_fd,
                        const SimulationConfig *config)
{

    /* ---- Handler de arranque (SIGUSR2) ---- */
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
    sigaction(SIGINT, &term_action, NULL);

    /* ---- US102: handler de violação (SIGUSR1) ----
     * Bloqueia TODOS os outros sinais durante a execução do handler,
     * conforme exigido pela US102 (acceptance criteria). */
    struct sigaction violation_action;
    memset(&violation_action, 0, sizeof(violation_action));
    violation_action.sa_handler = handle_violation;
    sigfillset(&violation_action.sa_mask);         /* bloqueia todos durante o handler */
    sigdelset(&violation_action.sa_mask, SIGKILL); /* SIGKILL não pode ser bloqueado */
    sigdelset(&violation_action.sa_mask, SIGSTOP); /* SIGSTOP também não */
    violation_action.sa_flags = SA_RESTART;
    sigaction(SIGUSR1, &violation_action, NULL);

    /* ---- Carregar o plano de voo ---- */
    FlightPlan plan;
    char err[256];
    if (parse_flight_plan(flight_file, &plan, err, sizeof(err)) != 0)
    {
        dprintf(STDERR_FILENO, "Flight process error: %s\n", err);
        close(update_write_fd);
        _exit(EXIT_FAILURE);
    }

    /* ---- Espera segura pelo sinal de arranque ----
     * SIGUSR2 foi bloqueado pelo pai antes do fork e essa máscara foi herdada
     * pelo filho. Com sigsuspend(), o filho desbloqueia temporariamente o sinal
     * enquanto dorme, evitando a race condition típica do pause(). */
    sigset_t wait_mask;
    sigprocmask(SIG_SETMASK, NULL, &wait_mask);
    sigdelset(&wait_mask, SIGUSR2);

    while (!start_requested && !terminate_requested)
    {
        sigsuspend(&wait_mask);
    }

    if (terminate_requested)
    {
        close(update_write_fd);
        close(sync_read_fd);
        _exit(EXIT_SUCCESS);
    }

    int departure_time = config->start_time;
    if (!config->start_time_provided &&
        parse_departure_time_seconds(flight_file, &departure_time, err, sizeof(err)) != 0)
    {
        dprintf(STDERR_FILENO, "Flight process error: %s\n", err);
        close(update_write_fd);
        close(sync_read_fd);
        _exit(EXIT_FAILURE);
    }

    /* Se a simulação global começar antes deste voo, o processo fica sincronizado
     * com o pai e só começa a emitir posições a partir da sua DepartureTime. */
    int current_time = config->start_time;
    while (current_time < departure_time && current_time <= config->end_time && !terminate_requested)
    {
        char command;
        ssize_t n = read(sync_read_fd, &command, sizeof(command));
        if (n <= 0 || command != 'N')
        {
            terminate_requested = 1;
            break;
        }
        current_time += config->step_seconds;
    }

    if (terminate_requested || current_time > config->end_time)
    {
        close(update_write_fd);
        close(sync_read_fd);
        _exit(EXIT_SUCCESS);
    }

        /* ---- Física simplificada ----
     * O movimento usa velocidades do perfil de voo e um fator meteorológico.
     * Isto evita uma interpolação puramente artificial e aproxima a simulação
     * do comportamento esperado no enunciado, sem implementar um modelo físico completo. */
    double weather_perf = weather_factor(config->weather_code);

        /* ---- Loop principal da simulação ---- */
    for (int i = 0; i < plan.segment_count && !terminate_requested; ++i)
    {
        Segment *seg = &plan.segments[i];

        double speed_knots = selected_speed_knots(&plan, seg) * weather_perf;
        if (speed_knots <= 0.0)
            speed_knots = 1.0;

        double segment_distance_nm = distance_nm(seg->start, seg->end);
        double segment_seconds = (segment_distance_nm / speed_knots) * 3600.0;

        double vertical_rate = selected_vertical_rate_ms(&plan, seg) * weather_perf;
        double altitude_delta = seg->end.altitude_m - seg->start.altitude_m;

        if (vertical_rate != 0.0)
        {
            double vertical_seconds = altitude_delta / vertical_rate;
            if (vertical_seconds < 0.0)
                vertical_seconds = -vertical_seconds;

            if (vertical_seconds > segment_seconds)
                segment_seconds = vertical_seconds;
        }

        int segment_steps = (int)(segment_seconds / config->step_seconds);
        if (segment_steps <= 0)
            segment_steps = 1;

        for (int s = 0;
             s <= segment_steps && current_time <= config->end_time && !terminate_requested;
             ++s)
        {
            double elapsed = (double)s * (double)config->step_seconds;
            double ratio = elapsed / segment_seconds;

            if (ratio > 1.0)
                ratio = 1.0;

            Position p = interpolate(seg->start, seg->end, ratio);

            if (vertical_rate != 0.0)
            {
                double altitude = seg->start.altitude_m + vertical_rate * elapsed;

                if (altitude_delta >= 0.0 && altitude > seg->end.altitude_m)
                    altitude = seg->end.altitude_m;

                if (altitude_delta < 0.0 && altitude < seg->end.altitude_m)
                    altitude = seg->end.altitude_m;

                p.altitude_m = altitude;
            }

            send_update(update_write_fd, &plan, current_time, p, STATUS_RUNNING);

            if (ratio >= 1.0)
                break;

            /* US103: aguarda autorização do pai para avançar de passo */
            char command;
            ssize_t n = read(sync_read_fd, &command, sizeof(command));
            if (n <= 0 || command != 'N')
            {
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