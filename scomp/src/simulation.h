#ifndef SIMULATION_H
#define SIMULATION_H

#include <sys/types.h>

/* Estado do voo durante a simulação. */
#define MAX_FLIGHTS 32
#define MAX_SEGMENTS 128
#define MAX_PATH 256
#define FLIGHT_ID_SIZE 64

#define STATUS_RUNNING 1
#define STATUS_FINISHED 2
#define STATUS_ABORTED 3

#define WEATHER_CLEAR 1
#define WEATHER_RAIN 2
#define WEATHER_STORM 3
#define WEATHER_FOG 4


/* Estrutura  para armazenar uma posição (lat, lon, altitude). */
typedef struct {
    double latitude;
    double longitude;
    double altitude_m;
} Position;

/* Cada segmento tem início/fim e um modo (ex: TAXI, CRUISE) */
typedef struct {
    Position start;
    Position end;
    char mode[16];
} Segment;

/* FlightPlan: guardo o id e a lista de segmentos. */
typedef struct {
    char flight_id[FLIGHT_ID_SIZE];
    int segment_count;
    Segment segments[MAX_SEGMENTS];
} FlightPlan;

/* PositionUpdate: formato enviado pelos processos filhos. */
typedef struct {
    int time_step;
    char flight_id[FLIGHT_ID_SIZE];
    pid_t pid;
    Position position;
    int status;
} PositionUpdate;

typedef struct {
    int time_step;
    Position position;
    int status;
} HistoryEntry;

/* FlightRuntime: estado de cada voo enquanto a simulação corre. */
typedef struct {
    char flight_id[FLIGHT_ID_SIZE];
    pid_t pid;
    int pipe_fd;
    int active;
    HistoryEntry *history;
    size_t history_count;
    size_t history_capacity;
} FlightRuntime;

typedef struct {
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
    int perf_max_flights;
    int start_time_provided;
} SimulationConfig;

int parse_args(int argc, char **argv, SimulationConfig *config);
int validate_config(const SimulationConfig *config);
int parse_flight_plan(const char *path, FlightPlan *plan, char *err, size_t err_size);
int parse_departure_time_seconds(const char *path, int *out_seconds, char *err, size_t err_size);
void run_flight_process(const char *flight_file, int write_fd, const SimulationConfig *config);
int append_history(FlightRuntime *runtime, const PositionUpdate *update);
int position_inside_area(Position p, const SimulationConfig *config);
void print_usage(const char *program);

#endif
