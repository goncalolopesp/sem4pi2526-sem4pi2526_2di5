#ifndef SIMULATION_H
#define SIMULATION_H

#include <sys/types.h>

/* ===== Limites e constantes ===== */
#define MAX_FLIGHTS 32
#define MAX_SEGMENTS 128
#define MAX_PATH 256
#define FLIGHT_ID_SIZE 64
#define MAX_TRACKED_FLIGHTS 64

/* Estados de execução de um voo */
#define STATUS_RUNNING 1
#define STATUS_FINISHED 2
#define STATUS_ABORTED 3

/* Códigos de meteorologia */
#define WEATHER_CLEAR 1
#define WEATHER_RAIN 2
#define WEATHER_STORM 3
#define WEATHER_FOG 4

/* ===== Estruturas básicas ===== */

typedef struct
{
    double latitude;
    double longitude;
    double altitude_m;
} Position;

typedef struct
{
    Position start;
    Position end;
    char mode[16];
} Segment;

typedef struct
{
    double climb_speed_knots;
    double descend_speed_knots;
    double cruise_speed_knots;

    double climb_rate_ms;
    double descent_rate_ms;

    double fuel_kg;

    char flight_id[FLIGHT_ID_SIZE];
    int segment_count;
    Segment segments[MAX_SEGMENTS];

} FlightPlan;

/* Update enviado pelos processos filhos para o pai via pipe */
typedef struct
{
    int time_step;
    char flight_id[FLIGHT_ID_SIZE];
    pid_t pid;
    Position position;
    int status;
} PositionUpdate;

/* ===== Position Tracker (US101) ===== */

typedef struct
{
    int time_step;
    Position position;
    int status;
} TrackPoint;

typedef struct
{
    char flight_id[FLIGHT_ID_SIZE];
    pid_t pid;
    Position current_position;
    int last_time_step;
    int status;
    TrackPoint *history;
    size_t history_count;
    size_t history_capacity;
} TrackEntry;

typedef struct
{
    TrackEntry entries[MAX_TRACKED_FLIGHTS];
    int entry_count;
} PositionTracker;

/* ===== Runtime no processo pai ===== */

typedef struct
{
    int time_step;
    Position position;
    int status;
} HistoryEntry;

typedef struct
{
    char flight_id[FLIGHT_ID_SIZE];
    pid_t pid;
    int pipe_fd; /* filho -> pai: PositionUpdate */
    int sync_fd; /* pai -> filho: autoriza próximo passo (US103) */
    int active;
    int departure_time; /* hora de partida do plano, em segundos */
    HistoryEntry *history;
    size_t history_count;
    size_t history_capacity;
} FlightRuntime;

/* ===== Configuração da simulação ===== */

typedef struct
{
    int start_time;
    int end_time;
    int step_seconds;
    double min_lat;
    double max_lat;
    double min_lon;
    double max_lon;
    int flight_count;
    char flight_files[MAX_FLIGHTS][MAX_PATH];
    int weather_code;
    double safety_min_sep_nm;
    double safety_min_alt_m;
    int violation_threshold;
    int perf_max_flights;
    char output_file[MAX_PATH];
    int start_time_provided;
} SimulationConfig;

/* ===== US102: Safety Violations ===== */
#define MAX_VIOLATIONS 1024
#define VIOLATION_THRESHOLD 10

typedef enum
{
    VIOLATION_SEPARATION = 1, /* dois aviões demasiado próximos */
    VIOLATION_ALTITUDE = 2    /* avião abaixo da altitude mínima */
} ViolationType;

typedef struct
{
    int time_step;
    ViolationType type;
    char flight_a[FLIGHT_ID_SIZE];
    char flight_b[FLIGHT_ID_SIZE]; /* vazio se for ALTITUDE */
    pid_t pid_a;
    pid_t pid_b;        /* 0 se for ALTITUDE */
    double distance_nm; /* só para SEPARATION */
    double altitude_m;  /* só para ALTITUDE */
} SafetyViolation;

typedef struct
{
    SafetyViolation events[MAX_VIOLATIONS];
    int count;
    int total_detected; /* pode crescer além do array */
} SafetyLog;

/* ===== Protótipos ===== */

int parse_args(int argc, char **argv, SimulationConfig *config);
int validate_config(const SimulationConfig *config);
int parse_flight_plan(const char *path, FlightPlan *plan, char *err, size_t err_size);
int parse_departure_time_seconds(const char *path, int *out_seconds, char *err, size_t err_size);

void run_flight_process(const char *flight_file,
                        int update_write_fd,
                        int sync_read_fd,
                        const SimulationConfig *config);

int append_history(FlightRuntime *runtime, const PositionUpdate *update);
int position_inside_area(Position p, const SimulationConfig *config);
void print_usage(const char *program);

/* Position Tracker */
void tracker_init(PositionTracker *tracker);
void tracker_destroy(PositionTracker *tracker);
int tracker_update(PositionTracker *tracker, const PositionUpdate *update);
const TrackEntry *tracker_get_current(const PositionTracker *tracker, const char *flight_id);
void tracker_print_summary(const PositionTracker *tracker);

/* Safety Monitor (US102) */
void safety_log_init(SafetyLog *log);
int safety_check_step(const PositionTracker *tracker,
                      const SimulationConfig *config,
                      int current_step,
                      SafetyLog *log,
                      const FlightRuntime *runtimes,
                      int runtime_count);
void safety_log_print(const SafetyLog *log);

#endif