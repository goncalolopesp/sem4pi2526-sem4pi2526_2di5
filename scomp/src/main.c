#define _POSIX_C_SOURCE 200809L
#include "simulation.h"
#include <ctype.h>
#include <errno.h>
#include <fcntl.h>
#include <poll.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>
#include <pthread.h>

static volatile sig_atomic_t stop_requested = 0;

static void handle_interrupt(int signo)
{
    (void)signo;
    stop_requested = 1;
}

static int equals_ignore_case(const char *a, const char *b)
{
    if (!a || !b)
        return 0;
    while (*a && *b)
    {
        unsigned char ca = (unsigned char)*a;
        unsigned char cb = (unsigned char)*b;
        if (tolower(ca) != tolower(cb))
            return 0;
        ++a;
        ++b;
    }
    return *a == '\0' && *b == '\0';
}

static int parse_weather_code(const char *value)
{
    if (equals_ignore_case(value, "clear"))
        return WEATHER_CLEAR;
    if (equals_ignore_case(value, "rain"))
        return WEATHER_RAIN;
    if (equals_ignore_case(value, "storm"))
        return WEATHER_STORM;
    if (equals_ignore_case(value, "fog"))
        return WEATHER_FOG;
    return 0;
}

void print_usage(const char *program)
{
    fprintf(stderr,
            "Usage: %s --area <minLat> <maxLat> <minLon> <maxLon> --start <sec> --end <sec> --step <sec> --weather <clear|rain|storm|fog> --safety-min-sep <nm> --safety-min-alt <m> --perf-max-flights <n> --flight <file> [--flight <file> ...]\n"
            "Example: %s --area 39 43 -9 -3 --end 3600 --step 60 --weather clear --safety-min-sep 3 --safety-min-alt 300 --perf-max-flights 10 --flight data/Flight_Plan_v0a.json\n",
            program, program);
}

int parse_args(int argc, char **argv, SimulationConfig *config)
{
    memset(config, 0, sizeof(*config));
    config->start_time = -1;
    config->start_time_provided = 0;
    config->step_seconds = 60;
    config->end_time = -1;
    config->weather_code = WEATHER_CLEAR;
    config->safety_min_sep_nm = 3.0;
    config->safety_min_alt_m = 300.0;
    config->violation_threshold = VIOLATION_THRESHOLD;
    config->perf_max_flights = MAX_FLIGHTS;
    snprintf(config->output_file, MAX_PATH, "simulation_report.txt");

    for (int i = 1; i < argc; ++i)
    {
        if (strcmp(argv[i], "--area") == 0 && i + 4 < argc)
        {
            config->min_lat = atof(argv[++i]);
            config->max_lat = atof(argv[++i]);
            config->min_lon = atof(argv[++i]);
            config->max_lon = atof(argv[++i]);
        }
        else if (strcmp(argv[i], "--start") == 0 && i + 1 < argc)
        {
            config->start_time = atoi(argv[++i]);
            config->start_time_provided = 1;
        }
        else if (strcmp(argv[i], "--end") == 0 && i + 1 < argc)
        {
            config->end_time = atoi(argv[++i]);
        }
        else if (strcmp(argv[i], "--step") == 0 && i + 1 < argc)
        {
            config->step_seconds = atoi(argv[++i]);
        }
        else if (strcmp(argv[i], "--weather") == 0 && i + 1 < argc)
        {
            config->weather_code = parse_weather_code(argv[++i]);
        }
        else if (strcmp(argv[i], "--safety-min-sep") == 0 && i + 1 < argc)
        {
            config->safety_min_sep_nm = atof(argv[++i]);
        }
        else if (strcmp(argv[i], "--safety-min-alt") == 0 && i + 1 < argc)
        {
            config->safety_min_alt_m = atof(argv[++i]);
        }
        else if (strcmp(argv[i], "--violation-threshold") == 0 && i + 1 < argc)
        {
            config->violation_threshold = atoi(argv[++i]);
        }
        else if (strcmp(argv[i], "--perf-max-flights") == 0 && i + 1 < argc)
        {
            config->perf_max_flights = atoi(argv[++i]);
        }
        else if (strcmp(argv[i], "--output") == 0 && i + 1 < argc)
        {
            snprintf(config->output_file, MAX_PATH, "%s", argv[++i]);
        }
        else if (strcmp(argv[i], "--flight") == 0 && i + 1 < argc)
        {
            if (config->flight_count >= MAX_FLIGHTS)
            {
                fprintf(stderr, "Too many flight files. Maximum: %d\n", MAX_FLIGHTS);
                return -1;
            }
            snprintf(config->flight_files[config->flight_count++], MAX_PATH, "%s", argv[++i]);
        }
        else
        {
            fprintf(stderr, "Invalid argument: %s\n", argv[i]);
            return -1;
        }
    }

    if (!config->start_time_provided && config->flight_count > 0)
    {
        char err[256];
        int seconds = 0;
        if (parse_departure_time_seconds(config->flight_files[0], &seconds, err, sizeof(err)) != 0)
        {
            fprintf(stderr, "Validation error: %s\n", err);
            return -1;
        }
        config->start_time = seconds;
        if (config->end_time >= 0 && config->end_time <= config->start_time)
        {
            config->end_time = config->start_time + config->end_time;
        }
    }

    return validate_config(config);
}

int validate_config(const SimulationConfig *config)
{
    if (config->flight_count <= 0)
    {
        fprintf(stderr, "Validation error: at least one --flight is required.\n");
        return -1;
    }
    if (config->start_time < 0)
    {
        fprintf(stderr, "Validation error: missing --start or DepartureTime in flight plan.\n");
        return -1;
    }
    if (config->end_time <= config->start_time)
    {
        fprintf(stderr, "Validation error: --end must be greater than --start.\n");
        return -1;
    }
    if (config->step_seconds <= 0)
    {
        fprintf(stderr, "Validation error: --step must be positive.\n");
        return -1;
    }
    if (config->weather_code == 0)
    {
        fprintf(stderr, "Validation error: --weather must be clear, rain, storm, or fog.\n");
        return -1;
    }
    if (config->safety_min_sep_nm <= 0.0)
    {
        fprintf(stderr, "Validation error: --safety-min-sep must be positive.\n");
        return -1;
    }
    if (config->safety_min_alt_m <= 0.0)
    {
        fprintf(stderr, "Validation error: --safety-min-alt must be positive.\n");
        return -1;
    }
    if (config->violation_threshold <= 0)
    {
        fprintf(stderr, "Validation error: violation threshold must be positive.\n");
        return 0;
    }
    if (config->perf_max_flights <= 0 || config->perf_max_flights > MAX_FLIGHTS)
    {
        fprintf(stderr, "Validation error: --perf-max-flights must be between 1 and %d.\n", MAX_FLIGHTS);
        return -1;
    }
    if (config->flight_count > config->perf_max_flights)
    {
        fprintf(stderr, "Validation error: number of flights exceeds --perf-max-flights.\n");
        return -1;
    }
    if (config->min_lat < -90 || config->max_lat > 90 || config->min_lat >= config->max_lat)
    {
        fprintf(stderr, "Validation error: invalid latitude range.\n");
        return -1;
    }
    if (config->min_lon < -180 || config->max_lon > 180 || config->min_lon >= config->max_lon)
    {
        fprintf(stderr, "Validation error: invalid longitude range.\n");
        return -1;
    }
    for (int i = 0; i < config->flight_count; ++i)
    {
        if (access(config->flight_files[i], R_OK) != 0)
        {
            fprintf(stderr, "Validation error: cannot read flight file '%s'.\n", config->flight_files[i]);
            return -1;
        }
    }
    return 0;
}

int position_inside_area(Position p, const SimulationConfig *config)
{
    return p.latitude >= config->min_lat && p.latitude <= config->max_lat &&
           p.longitude >= config->min_lon && p.longitude <= config->max_lon;
}

int append_history(FlightRuntime *runtime, const PositionUpdate *update)
{
    if (runtime->history_count == runtime->history_capacity)
    {
        size_t new_capacity = runtime->history_capacity == 0 ? 16 : runtime->history_capacity * 2;
        HistoryEntry *new_history = realloc(runtime->history, new_capacity * sizeof(*new_history));
        if (!new_history)
            return -1;
        runtime->history = new_history;
        runtime->history_capacity = new_capacity;
    }
    HistoryEntry *entry = &runtime->history[runtime->history_count++];
    entry->time_step = update->time_step;
    entry->position = update->position;
    entry->status = update->status;
    return 0;
}

static void free_runtimes(FlightRuntime *runtimes, int count)
{
    for (int i = 0; i < count; ++i)
        free(runtimes[i].history);
}

static void deactivate_runtime(FlightRuntime *runtime, struct pollfd *poll_fd)
{
    if (!runtime || !runtime->active)
        return;

    if (runtime->pipe_fd >= 0)
    {
        close(runtime->pipe_fd);
        runtime->pipe_fd = -1;
    }

    runtime->active = 0;
    if (poll_fd)
    {
        poll_fd->fd = -1;
        poll_fd->events = 0;
        poll_fd->revents = 0;
    }
}

static void write_report(const FlightRuntime *runtimes, int count,
                         const SimulationConfig *config,
                         const PositionTracker *tracker,
                         const SafetyLog *safety_log)
{
    FILE *report = fopen(config->output_file, "w");
    if (!report)
        return;

    fprintf(report, "US100/US101/US102/US107 Simulation Report\n");
    fprintf(report, "Area: lat [%.6f, %.6f], lon [%.6f, %.6f]\n",
            config->min_lat, config->max_lat, config->min_lon, config->max_lon);
    fprintf(report, "Time range: %d to %d, step: %d seconds\n",
            config->start_time, config->end_time, config->step_seconds);
    fprintf(report, "Safety thresholds: min separation = %.2f NM, min altitude = %.1f m\n",
            config->safety_min_sep_nm, config->safety_min_alt_m);
    fprintf(report, "Violation threshold: %d\n", config->violation_threshold);
    fprintf(report, "Total flights: %d\n\n", count);

    /* Secção 1: histórico por runtime (US100) */
    for (int i = 0; i < count; ++i)
    {
        fprintf(report, "Flight %s, PID %d, updates %zu\n",
                runtimes[i].flight_id, runtimes[i].pid, runtimes[i].history_count);
        for (size_t j = 0; j < runtimes[i].history_count; ++j)
        {
            const HistoryEntry *h = &runtimes[i].history[j];
            fprintf(report, "  t=%d lat=%.6f lon=%.6f alt=%.2f status=%d\n",
                    h->time_step, h->position.latitude, h->position.longitude,
                    h->position.altitude_m, h->status);
        }
        fprintf(report, "\n");
    }

    /* Secção 2: resumo do Position Tracker (US101) */
    fprintf(report, "=== US101 Position Tracker (%d entries) ===\n", tracker->entry_count);
    for (int i = 0; i < tracker->entry_count; ++i)
    {
        const TrackEntry *e = &tracker->entries[i];
        const char *st = e->status == STATUS_RUNNING ? "RUNNING" : e->status == STATUS_FINISHED ? "FINISHED"
                                                               : e->status == STATUS_ABORTED    ? "ABORTED"
                                                                                                : "UNKNOWN";
        fprintf(report,
                "  [%s] pid=%d final_pos=(%.6f, %.6f, %.1fm) status=%s history=%zu pts\n",
                e->flight_id, e->pid,
                e->current_position.latitude,
                e->current_position.longitude,
                e->current_position.altitude_m,
                st, e->history_count);

        for (size_t j = 0; j < e->history_count; ++j)
        {
            const TrackPoint *tp = &e->history[j];
            fprintf(report, "    t=%d lat=%.6f lon=%.6f alt=%.2f status=%d\n",
                    tp->time_step,
                    tp->position.latitude,
                    tp->position.longitude,
                    tp->position.altitude_m,
                    tp->status);
        }
    }
    fprintf(report, "==========================================\n\n");

    /* Secção 3: violações de segurança (US102) */
    fprintf(report, "=== US102 Safety Violations (%d shown / %d total) ===\n",
            safety_log->count, safety_log->total_detected);
    if (safety_log->total_detected > safety_log->count)
    {
        fprintf(report, "(NOTE: %d additional violations occurred but were not stored due to MAX_VIOLATIONS limit)\n",
                safety_log->total_detected - safety_log->count);
    }
    for (int i = 0; i < safety_log->count; ++i)
    {
        const SafetyViolation *v = &safety_log->events[i];
        if (v->type == VIOLATION_ALTITUDE)
        {
            fprintf(report, "  t=%d ALTITUDE  flight=%s pid=%d alt=%.1fm (min=%.1fm)\n",
                    v->time_step, v->flight_a, v->pid_a,
                    v->altitude_m, config->safety_min_alt_m);
        }
        else
        {
            fprintf(report, "  t=%d SEPARATION %s(pid=%d) <-> %s(pid=%d) dist=%.2f NM (min=%.2f NM)\n",
                    v->time_step,
                    v->flight_a, v->pid_a,
                    v->flight_b, v->pid_b,
                    v->distance_nm, config->safety_min_sep_nm);
        }
    }
    fprintf(report, "==========================================\n");

    int failed = 0;

    if (safety_log->total_detected >= config->violation_threshold)
    {
        failed = 1;
    }

    for (int i = 0; i < tracker->entry_count; ++i)
    {
        if (tracker->entries[i].status == STATUS_ABORTED)
        {
            failed = 1;
            break;
        }
    }

    fprintf(report, "\n=== FINAL VALIDATION RESULT ===\n");
    fprintf(report, "Simulation result: %s\n",
            failed ? "FAIL" : "PASS");
    fprintf(report, "================================\n");

    fclose(report);
}

int main(int argc, char **argv)
{
    SimulationConfig config;
    if (parse_args(argc, argv, &config) != 0)
    {
        print_usage(argv[0]);
        return EXIT_FAILURE;
    }

    /* Handler para CTRL-C / SIGTERM no processo pai */
    struct sigaction interrupt_action;
    memset(&interrupt_action, 0, sizeof(interrupt_action));
    interrupt_action.sa_handler = handle_interrupt;
    sigemptyset(&interrupt_action.sa_mask);
    interrupt_action.sa_flags = SA_RESTART;
    sigaction(SIGINT, &interrupt_action, NULL);
    sigaction(SIGTERM, &interrupt_action, NULL);

    PositionTracker tracker;
    tracker_init(&tracker);

    SharedSimulationState *shared =
        shared_create();

    /* US102: log de violações de segurança */
    SafetyLog safety_log;
    safety_log_init(&safety_log);

    FlightRuntime runtimes[MAX_FLIGHTS];
    memset(runtimes, 0, sizeof(runtimes));

    /* US106/US107: threads de deteção e relatório em tempo real */
    SimulationThreadContext thread_ctx;
    simulation_threads_init(&thread_ctx, shared, &config, &safety_log,
                            runtimes, config.flight_count);

    pthread_t safety_thread;
    pthread_t report_thread;

    pthread_create(&safety_thread, NULL, safety_detection_thread, &thread_ctx);
    pthread_create(&report_thread, NULL, report_generation_thread, &thread_ctx);

    struct pollfd poll_fds[MAX_FLIGHTS];
    memset(poll_fds, 0, sizeof(poll_fds));

    printf("Starting simulation with %d flight(s).\n", config.flight_count);
    printf("Safety thresholds: separation=%.2f NM, altitude=%.1f m, threshold=%d.\n",
           config.safety_min_sep_nm,
           config.safety_min_alt_m,
           config.violation_threshold);

    /*
     * Bloqueia SIGUSR2 antes do fork.
     * Os filhos herdam a máscara, instalam o handler e esperam com sigsuspend().
     * Assim evitamos perder o sinal de arranque se o pai o enviar demasiado cedo.
     */
    sigset_t start_mask;
    sigset_t previous_mask;
    sigemptyset(&start_mask);
    sigaddset(&start_mask, SIGUSR2);
    if (sigprocmask(SIG_BLOCK, &start_mask, &previous_mask) != 0)
    {
        perror("sigprocmask");
        tracker_destroy(&tracker);
        return EXIT_FAILURE;
    }

    /* Criação dos processos filhos */
    for (int i = 0; i < config.flight_count; ++i)
    {
        int update_pipe[2]; /* filho -> pai */
        int departure_time = config.start_time;
        char departure_err[256];

        if (!config.start_time_provided &&
            parse_departure_time_seconds(config.flight_files[i],
                                         &departure_time,
                                         departure_err,
                                         sizeof(departure_err)) != 0)
        {
            fprintf(stderr, "Validation error: %s\n", departure_err);
            sigprocmask(SIG_SETMASK, &previous_mask, NULL);
            free_runtimes(runtimes, i);
            tracker_destroy(&tracker);
            return EXIT_FAILURE;
        }

        if (pipe(update_pipe) != 0)
        {
            perror("pipe update_pipe");
            sigprocmask(SIG_SETMASK, &previous_mask, NULL);
            free_runtimes(runtimes, i);
            tracker_destroy(&tracker);
            return EXIT_FAILURE;
        }

        pid_t pid = fork();
        if (pid < 0)
        {
            perror("fork");
            close(update_pipe[0]);
            close(update_pipe[1]);
            sigprocmask(SIG_SETMASK, &previous_mask, NULL);
            free_runtimes(runtimes, i);
            tracker_destroy(&tracker);
            return EXIT_FAILURE;
        }

        if (pid == 0)
        {
            /* Filho */
            close(update_pipe[0]); /* não lê updates */
            run_flight_process(config.flight_files[i], i, update_pipe[1], &config);
            /* run_flight_process termina o processo com _exit() */
        }

        /* Pai */
        close(update_pipe[1]); /* não escreve updates */

        runtimes[i].pid = pid;
        runtimes[i].pipe_fd = update_pipe[0];
        runtimes[i].active = 1;
        runtimes[i].departure_time = departure_time;

        /* Nome temporário; é atualizado quando o primeiro update chega
           (no filho, o flight_id passou a incluir o PID). */
        snprintf(runtimes[i].flight_id, sizeof(runtimes[i].flight_id),
                 "flight-%d", i + 1);

        poll_fds[i].fd = update_pipe[0];
        poll_fds[i].events = POLLIN | POLLHUP;
    }

    /* Pai volta à máscara original antes de enviar o sinal de arranque. */
    sigprocmask(SIG_SETMASK, &previous_mask, NULL);

    /* US100/US103: dispara o arranque. SIGUSR1 está reservado para violações. */
    for (int i = 0; i < config.flight_count; ++i)
    {
        kill(runtimes[i].pid, SIGUSR2);
    }

    int active = config.flight_count;

    /* Loop principal: passo a passo */
    for (int current_step = config.start_time;
         current_step <= config.end_time && active > 0 && !stop_requested;
         current_step += config.step_seconds)
    {

        int expected = 0;
        for (int i = 0; i < config.flight_count; ++i)
        {
            if (runtimes[i].active && runtimes[i].departure_time <= current_step)
            {
                expected++;
            }
        }
        int received = 0;

        /* Recolhe um update por cada voo ativo neste passo */
        while (received < expected && !stop_requested)
        {
            int ready = poll(poll_fds, (nfds_t)config.flight_count, -1);
            if (ready < 0)
            {
                if (errno == EINTR)
                    continue;
                perror("poll");
                break;
            }

            for (int i = 0; i < config.flight_count; ++i)
            {
                if (!runtimes[i].active || runtimes[i].departure_time > current_step)
                    continue;

                if (poll_fds[i].revents & POLLIN)
                {
                    PositionUpdate update;
                    ssize_t n = read(runtimes[i].pipe_fd, &update, sizeof(update));
                    if (n == (ssize_t)sizeof(update))
                    {
                        /* Atualiza o flight_id real (vem com PID anexado) */
                        snprintf(runtimes[i].flight_id, sizeof(runtimes[i].flight_id),
                                 "%s", update.flight_id);

                        append_history(&runtimes[i], &update);
                        tracker_update(&tracker, &update);

                        const char *scope = position_inside_area(update.position, &config)
                                                ? "inside"
                                                : "outside";
                        printf("[%s] step=%d pid=%d lat=%.6f lon=%.6f alt=%.1f status=%d (%s area)\n",
                               update.flight_id, update.time_step, update.pid,
                               update.position.latitude, update.position.longitude,
                               update.position.altitude_m, update.status, scope);

                        received++;

                        if (update.status == STATUS_FINISHED || update.status == STATUS_ABORTED)
                        {
                            deactivate_runtime(&runtimes[i], &poll_fds[i]);
                            active--;
                            if (expected > 0)
                                expected--;
                        }
                    }
                    else if (n == 0)
                    {
                        deactivate_runtime(&runtimes[i], &poll_fds[i]);
                        active--;
                        if (expected > 0)
                            expected--;
                    }
                }

                if (runtimes[i].active && (poll_fds[i].revents & POLLHUP))
                {
                    deactivate_runtime(&runtimes[i], &poll_fds[i]);
                    active--;
                    if (expected > 0)
                        expected--;
                }

                poll_fds[i].revents = 0;
            }
        }

        /* US107: deteção de violações delegada à safety thread via shared memory */
        pthread_mutex_lock(&thread_ctx.report_mutex);
        int total_violations = safety_log.total_detected;
        pthread_mutex_unlock(&thread_ctx.report_mutex);

        if (total_violations > 0)
        {
            fprintf(stderr, "[ATC] step=%d: total safety violations=%d\n",
                    current_step, total_violations);
        }

        /* Threshold: termina antecipadamente se exceder */
        if (total_violations >= config.violation_threshold)
        {
            fprintf(stderr,
                    "[ATC] Violation threshold (%d) exceeded. Aborting simulation.\n",
                    config.violation_threshold);
            stop_requested = 1;
        }

        /* US103: liberta o próximo passo para os filhos ainda ativos */
        if (!stop_requested)
        {
            for (int i = 0; i < config.flight_count; ++i)
            {
                if (runtimes[i].active)
                {
                    sem_post(&shared->step_start[i]);
                }
            }
        }
    }

    /* Termina filhos ainda vivos */
    if (stop_requested)
    {
        fprintf(stderr, "Termination requested. Stopping flight processes...\n");
        for (int i = 0; i < config.flight_count; ++i)
        {
            if (runtimes[i].active)
                kill(runtimes[i].pid, SIGTERM);
        }
    }

    /* Sinaliza terminação global e desbloqueia todos os filhos
       que possam estar bloqueados à espera do próximo timestep. */
    shared->terminate = 1;

    for (int i = 0; i < config.flight_count; ++i)
    {
        /* Liberta o próximo timestep apenas depois de o pai
           terminar o processamento do passo atual. */
        sem_post(&shared->step_start[i]);

        if (runtimes[i].active && runtimes[i].pipe_fd >= 0)
        {
            close(runtimes[i].pipe_fd);
            runtimes[i].pipe_fd = -1;
        }
    }

    /* Recolhe todos os filhos para evitar zombies */
    for (int i = 0; i < config.flight_count; ++i)
    {
        int status;
        waitpid(runtimes[i].pid, &status, 0);
    }

    for (int i = 0; i < tracker.entry_count; ++i)
    {
        if (tracker.entries[i].status == STATUS_RUNNING)
        {
            tracker.entries[i].status = STATUS_FINISHED;
        }
    }

    /* Resumos no terminal */
    tracker_print_summary(&tracker);
    safety_log_print(&safety_log);

    /* Relatório completo em ficheiro */
    write_report(runtimes, config.flight_count, &config, &tracker, &safety_log);

    tracker_destroy(&tracker);
    free_runtimes(runtimes, config.flight_count);

    stop_requested = 1;
    thread_ctx.stop = 1;

    pthread_mutex_lock(&shared->pthread_mutex);
    pthread_cond_broadcast(&shared->pthread_cond);
    pthread_mutex_unlock(&shared->pthread_mutex);

    pthread_mutex_lock(&thread_ctx.report_mutex);
    pthread_cond_signal(&thread_ctx.violation_cond);
    pthread_mutex_unlock(&thread_ctx.report_mutex);

    pthread_join(safety_thread, NULL);
    pthread_join(report_thread, NULL);

    simulation_threads_destroy(&thread_ctx);

    shared_destroy(shared);

    printf("Simulation finished. Report saved in %s\n", config.output_file);
    printf("Real-time violation log saved in %s\n", thread_ctx.realtime_log_path);
    return EXIT_SUCCESS;
}