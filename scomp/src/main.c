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

/* Programa principal: cria processos filhos, sincroniza o arranque com SIGUSR1 e recolhe updates via pipes. */
static volatile sig_atomic_t stop_requested = 0;

static void handle_interrupt(int signo) {
    (void)signo;
    stop_requested = 1;
}

static int equals_ignore_case(const char *a, const char *b) {
    if (!a || !b) return 0;
    while (*a && *b) {
        unsigned char ca = (unsigned char)*a;
        unsigned char cb = (unsigned char)*b;
        if (tolower(ca) != tolower(cb)) return 0;
        ++a;
        ++b;
    }
    return *a == '\0' && *b == '\0';
}

static int parse_weather_code(const char *value) {
    if (equals_ignore_case(value, "clear")) return WEATHER_CLEAR;
    if (equals_ignore_case(value, "rain")) return WEATHER_RAIN;
    if (equals_ignore_case(value, "storm")) return WEATHER_STORM;
    if (equals_ignore_case(value, "fog")) return WEATHER_FOG;
    return 0;
}

/* Handler para ctrl-c, marco pedido de paragem para terminar filhos com SIGTERM. */
void print_usage(const char *program) {
    fprintf(stderr,
        "Usage: %s --area <minLat> <maxLat> <minLon> <maxLon> --start <sec> --end <sec> --step <sec> --weather <clear|rain|storm|fog> --safety-min-sep <nm> --safety-min-alt <m> --perf-max-flights <n> --flight <file> [--flight <file> ...]\n"
        "Note: if --start is omitted, DepartureTime from the first flight plan is used.\n"
        "Example: %s --area 39 43 -9 -3 --end 3600 --step 60 --weather clear --safety-min-sep 3 --safety-min-alt 300 --perf-max-flights 10 --flight data/Flight_Plan_v0a.json\n",
        program, program);
}

/* Funções auxiliares para argumentos e validação. */
int parse_args(int argc, char **argv, SimulationConfig *config) {
    memset(config, 0, sizeof(*config));
    config->start_time = -1;
    config->start_time_provided = 0;
    config->step_seconds = 60;
    config->end_time = -1;
    config->weather_code = WEATHER_CLEAR;
    config->safety_min_sep_nm = 3.0;
    config->safety_min_alt_m = 300.0;
    config->perf_max_flights = MAX_FLIGHTS;

    for (int i = 1; i < argc; ++i) {
        if (strcmp(argv[i], "--area") == 0 && i + 4 < argc) {
            config->min_lat = atof(argv[++i]);
            config->max_lat = atof(argv[++i]);
            config->min_lon = atof(argv[++i]);
            config->max_lon = atof(argv[++i]);
        } else if (strcmp(argv[i], "--start") == 0 && i + 1 < argc) {
            config->start_time = atoi(argv[++i]);
            config->start_time_provided = 1;
        } else if (strcmp(argv[i], "--end") == 0 && i + 1 < argc) {
            config->end_time = atoi(argv[++i]);
        } else if (strcmp(argv[i], "--step") == 0 && i + 1 < argc) {
            config->step_seconds = atoi(argv[++i]);
        } else if (strcmp(argv[i], "--weather") == 0 && i + 1 < argc) {
            config->weather_code = parse_weather_code(argv[++i]);
        } else if (strcmp(argv[i], "--safety-min-sep") == 0 && i + 1 < argc) {
            config->safety_min_sep_nm = atof(argv[++i]);
        } else if (strcmp(argv[i], "--safety-min-alt") == 0 && i + 1 < argc) {
            config->safety_min_alt_m = atof(argv[++i]);
        } else if (strcmp(argv[i], "--perf-max-flights") == 0 && i + 1 < argc) {
            config->perf_max_flights = atoi(argv[++i]);
        } else if (strcmp(argv[i], "--flight") == 0 && i + 1 < argc) {
            if (config->flight_count >= MAX_FLIGHTS) {
                fprintf(stderr, "Too many flight files. Maximum: %d\n", MAX_FLIGHTS);
                return -1;
            }
            snprintf(config->flight_files[config->flight_count++], MAX_PATH, "%s", argv[++i]);
        } else {
            fprintf(stderr, "Invalid argument: %s\n", argv[i]);
            return -1;
        }
    }

    if (!config->start_time_provided && config->flight_count > 0) {
        char err[256];
        int seconds = 0;
        if (parse_departure_time_seconds(config->flight_files[0], &seconds, err, sizeof(err)) != 0) {
            fprintf(stderr, "Validation error: %s\n", err);
            return -1;
        }
        config->start_time = seconds;
        if (config->end_time >= 0 && config->end_time <= config->start_time) {
            config->end_time = config->start_time + config->end_time;
        }
    }

    return validate_config(config);
}

int validate_config(const SimulationConfig *config) {
    if (config->flight_count <= 0) {
        fprintf(stderr, "Validation error: at least one --flight is required.\n");
        return -1;
    }
    if (config->start_time < 0) {
        fprintf(stderr, "Validation error: missing --start or DepartureTime in flight plan.\n");
        return -1;
    }
    if (config->end_time <= config->start_time) {
        fprintf(stderr, "Validation error: --end must be greater than --start.\n");
        return -1;
    }
    if (config->step_seconds <= 0) {
        fprintf(stderr, "Validation error: --step must be positive.\n");
        return -1;
    }
    if (config->weather_code == 0) {
        fprintf(stderr, "Validation error: --weather must be clear, rain, storm, or fog.\n");
        return -1;
    }
    if (config->safety_min_sep_nm <= 0.0) {
        fprintf(stderr, "Validation error: --safety-min-sep must be positive.\n");
        return -1;
    }
    if (config->safety_min_alt_m <= 0.0) {
        fprintf(stderr, "Validation error: --safety-min-alt must be positive.\n");
        return -1;
    }
    if (config->perf_max_flights <= 0 || config->perf_max_flights > MAX_FLIGHTS) {
        fprintf(stderr, "Validation error: --perf-max-flights must be between 1 and %d.\n", MAX_FLIGHTS);
        return -1;
    }
    if (config->flight_count > config->perf_max_flights) {
        fprintf(stderr, "Validation error: number of flights exceeds --perf-max-flights.\n");
        return -1;
    }
    if (config->min_lat < -90 || config->max_lat > 90 || config->min_lat >= config->max_lat) {
        fprintf(stderr, "Validation error: invalid latitude range.\n");
        return -1;
    }
    if (config->min_lon < -180 || config->max_lon > 180 || config->min_lon >= config->max_lon) {
        fprintf(stderr, "Validation error: invalid longitude range.\n");
        return -1;
    }
    for (int i = 0; i < config->flight_count; ++i) {
        if (access(config->flight_files[i], R_OK) != 0) {
            fprintf(stderr, "Validation error: cannot read flight file '%s'.\n", config->flight_files[i]);
            return -1;
        }
    }
    return 0;
}

int position_inside_area(Position p, const SimulationConfig *config) {
    return p.latitude >= config->min_lat && p.latitude <= config->max_lat &&
           p.longitude >= config->min_lon && p.longitude <= config->max_lon;
}

/* Verifica se a posição está dentro da área. */
int append_history(FlightRuntime *runtime, const PositionUpdate *update) {
    if (runtime->history_count == runtime->history_capacity) {
        size_t new_capacity = runtime->history_capacity == 0 ? 16 : runtime->history_capacity * 2;
        HistoryEntry *new_history = realloc(runtime->history, new_capacity * sizeof(*new_history));
        if (!new_history) return -1;
        runtime->history = new_history;
        runtime->history_capacity = new_capacity;
    }
    HistoryEntry *entry = &runtime->history[runtime->history_count++];
    entry->time_step = update->time_step;
    entry->position = update->position;
    entry->status = update->status;
    return 0;
}

/* Guardar histórico de updates no runtime para gerar o ficheiro final. */
static void free_runtimes(FlightRuntime *runtimes, int count) {
    for (int i = 0; i < count; ++i) free(runtimes[i].history);
}

static void write_report(const FlightRuntime *runtimes, int count,
                         const SimulationConfig *config,
                         const PositionTracker *tracker) {
    FILE *report = fopen("simulation_report.txt", "w");
    if (!report) return;

    fprintf(report, "US100/US101 Simulation Report\n");
    fprintf(report, "Area: lat [%.6f, %.6f], lon [%.6f, %.6f]\n",
            config->min_lat, config->max_lat, config->min_lon, config->max_lon);
    fprintf(report, "Time range: %d to %d, step: %d seconds\n",
            config->start_time, config->end_time, config->step_seconds);
    fprintf(report, "Total flights: %d\n\n", count);

    /* Secção 1: histórico por runtime (US100). */
    for (int i = 0; i < count; ++i) {
        fprintf(report, "Flight %s, PID %d, updates %zu\n",
                runtimes[i].flight_id, runtimes[i].pid, runtimes[i].history_count);
        for (size_t j = 0; j < runtimes[i].history_count; ++j) {
            const HistoryEntry *h = &runtimes[i].history[j];
            fprintf(report, "  t=%d lat=%.6f lon=%.6f alt=%.2f status=%d\n",
                    h->time_step, h->position.latitude, h->position.longitude,
                    h->position.altitude_m, h->status);
        }
        fprintf(report, "\n");
    }

    /* Secção 2: resumo do Position Tracker (US101). */
    fprintf(report, "=== US101 Position Tracker (%d entries) ===\n", tracker->entry_count);
    for (int i = 0; i < tracker->entry_count; ++i) {
        const TrackEntry *e = &tracker->entries[i];
        const char *st = e->status == STATUS_RUNNING  ? "RUNNING"  :
                         e->status == STATUS_FINISHED ? "FINISHED" :
                         e->status == STATUS_ABORTED  ? "ABORTED"  : "UNKNOWN";
        fprintf(report,
                "  [%s] pid=%d final_pos=(%.6f, %.6f, %.1fm) status=%s history=%zu pts\n",
                e->flight_id, e->pid,
                e->current_position.latitude,
                e->current_position.longitude,
                e->current_position.altitude_m,
                st, e->history_count);

        /* Lista completa do histórico para cada voo (útil para US102). */
        for (size_t j = 0; j < e->history_count; ++j) {
            const TrackPoint *tp = &e->history[j];
            fprintf(report, "    t=%d lat=%.6f lon=%.6f alt=%.2f status=%d\n",
                    tp->time_step,
                    tp->position.latitude,
                    tp->position.longitude,
                    tp->position.altitude_m,
                    tp->status);
        }
    }
    fprintf(report, "==========================================\n");
    fclose(report);
}

/* Escrevo um relatório em texto. */
int main(int argc, char **argv) {
    SimulationConfig config;
    if (parse_args(argc, argv, &config) != 0) {
        print_usage(argv[0]);
        return EXIT_FAILURE;
    }

    struct sigaction interrupt_action;
    memset(&interrupt_action, 0, sizeof(interrupt_action));
    interrupt_action.sa_handler = handle_interrupt;
    sigemptyset(&interrupt_action.sa_mask);
    interrupt_action.sa_flags = SA_RESTART;
    sigaction(SIGINT, &interrupt_action, NULL);
    sigaction(SIGTERM, &interrupt_action, NULL);

    PositionTracker tracker;
    tracker_init(&tracker);

    FlightRuntime runtimes[MAX_FLIGHTS];
    memset(runtimes, 0, sizeof(runtimes));
    struct pollfd poll_fds[MAX_FLIGHTS];
    memset(poll_fds, 0, sizeof(poll_fds));

    printf("Starting US100 simulation with %d flight(s).\n", config.flight_count);

    sigset_t startup_mask;
    sigemptyset(&startup_mask);
    sigaddset(&startup_mask, SIGUSR1);
    sigprocmask(SIG_BLOCK, &startup_mask, NULL);

    for (int i = 0; i < config.flight_count; ++i) {
        int fd[2];
        if (pipe(fd) != 0) {
            perror("pipe");
            free_runtimes(runtimes, i);
            return EXIT_FAILURE;
        }

        pid_t pid = fork();
        if (pid < 0) {
            perror("fork");
            close(fd[0]);
            close(fd[1]);
            free_runtimes(runtimes, i);
            return EXIT_FAILURE;
        }

        if (pid == 0) {
            close(fd[0]);
            run_flight_process(config.flight_files[i], fd[1], &config);
        }

        close(fd[1]);
        runtimes[i].pid = pid;
        runtimes[i].pipe_fd = fd[0];
        runtimes[i].active = 1;
        snprintf(runtimes[i].flight_id, sizeof(runtimes[i].flight_id), "flight-%d", i + 1);
        poll_fds[i].fd = fd[0];
        poll_fds[i].events = POLLIN | POLLHUP;
    }

    for (int i = 0; i < config.flight_count; ++i) {
        kill(runtimes[i].pid, SIGUSR1);
    }
    sigprocmask(SIG_UNBLOCK, &startup_mask, NULL);

    int active = config.flight_count;
    while (active > 0 && !stop_requested) {
        int ready = poll(poll_fds, (nfds_t)config.flight_count, 1000);
        if (ready < 0) {
            if (errno == EINTR) continue;
            perror("poll");
            break;
        }
        if (ready == 0) continue;

        for (int i = 0; i < config.flight_count; ++i) {
            if (!runtimes[i].active) continue;
            if (poll_fds[i].revents & POLLIN) {
                PositionUpdate update;
                ssize_t n = read(runtimes[i].pipe_fd, &update, sizeof(update));
                if (n == (ssize_t)sizeof(update)) {
                    if (strcmp(runtimes[i].flight_id, "flight-1") == 0 || strncmp(runtimes[i].flight_id, "flight-", 7) == 0) {
                        snprintf(runtimes[i].flight_id, sizeof(runtimes[i].flight_id), "%s", update.flight_id);
                    }
                    append_history(&runtimes[i], &update);
                    tracker_update(&tracker, &update);
                    const char *scope = position_inside_area(update.position, &config) ? "inside" : "outside";
                    printf("[%s] t=%d pid=%d lat=%.6f lon=%.6f alt=%.1f status=%d (%s area)\n",
                           update.flight_id, update.time_step, update.pid,
                           update.position.latitude, update.position.longitude,
                           update.position.altitude_m, update.status, scope);
                } else if (n == 0) {
                    close(runtimes[i].pipe_fd);
                    runtimes[i].active = 0;
                    poll_fds[i].fd = -1;
                    active--;
                }
            }
            if (poll_fds[i].revents & POLLHUP) {
                close(runtimes[i].pipe_fd);
                runtimes[i].active = 0;
                poll_fds[i].fd = -1;
                active--;
            }
        }
    }

    if (stop_requested) {
        fprintf(stderr, "Termination requested. Stopping flight processes...\n");
        for (int i = 0; i < config.flight_count; ++i) {
            if (runtimes[i].active) kill(runtimes[i].pid, SIGTERM);
        }
    }

    for (int i = 0; i < config.flight_count; ++i) {
        int status;
        waitpid(runtimes[i].pid, &status, 0);
    }

    // write_report(runtimes, config.flight_count, &config);
    tracker_print_summary(&tracker);
    write_report(runtimes, config.flight_count, &config, &tracker);
    tracker_destroy(&tracker);
    free_runtimes(runtimes, config.flight_count);
    printf("Simulation finished. Report saved in simulation_report.txt\n");
    return EXIT_SUCCESS;
}