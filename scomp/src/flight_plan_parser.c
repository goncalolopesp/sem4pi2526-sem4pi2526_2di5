#include <ctype.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "simulation.h"

/* Parser JSON, escrito à mão para evitar dependências. */
static char *read_file(const char *path, long *size_out)
{
    FILE *file = fopen(path, "rb");
    if (!file)
        return NULL;
    if (fseek(file, 0, SEEK_END) != 0)
    {
        fclose(file);
        return NULL;
    }
    long size = ftell(file);
    if (size < 0)
    {
        fclose(file);
        return NULL;
    }
    rewind(file);
    char *buffer = calloc((size_t)size + 1, 1);
    if (!buffer)
    {
        fclose(file);
        return NULL;
    }
    size_t read_bytes = fread(buffer, 1, (size_t)size, file);
    fclose(file);
    buffer[read_bytes] = '\0';
    if (size_out)
        *size_out = (long)read_bytes;
    return buffer;
}

/* Leio todo o ficheiro para memória. */
static char *find_matching_brace(char *open_brace)
{
    int depth = 0;
    for (char *p = open_brace; *p; ++p)
    {
        if (*p == '{')
            depth++;
        else if (*p == '}')
        {
            depth--;
            if (depth == 0)
                return p;
        }
    }
    return NULL;
}

/* Procuro a '}' que fecha o bloco, contando profundidade */
static int extract_string_after_key(char *start, const char *key, char *out, size_t out_size)
{
    char pattern[64];
    snprintf(pattern, sizeof(pattern), "\"%s\"", key);
    char *p = strstr(start, pattern);
    if (!p)
        return -1;
    p = strchr(p + strlen(pattern), ':');
    if (!p)
        return -1;
    p++;
    while (*p && isspace((unsigned char)*p))
        p++;
    if (*p != '"')
        return -1;
    p++;
    char *q = strchr(p, '"');
    if (!q)
        return -1;
    size_t len = (size_t)(q - p);
    if (len >= out_size)
        len = out_size - 1;
    memcpy(out, p, len);
    out[len] = '\0';
    return 0;
}

/* Extraio strings do pseudo-JSON (ex: "Mode": "CRUISE"). */
static int extract_number_after_key(char *start, const char *key, double *value)
{
    char pattern[64];
    snprintf(pattern, sizeof(pattern), "\"%s\"", key);
    char *p = strstr(start, pattern);
    if (!p)
        return -1;
    p = strchr(p + strlen(pattern), ':');
    if (!p)
        return -1;
    p++;
    while (*p && (isspace((unsigned char)*p) || *p == '{'))
        p++;
    errno = 0;
    char *end = NULL;
    double number = strtod(p, &end);
    if (p == end || errno != 0)
        return -1;
    *value = number;
    return 0;
}

/* Extraio números com strtod; valido erros para poder reportar no parser. */
static int extract_altitude_quantity(char *block, double *value)
{
    char *alt = strstr(block, "\"Altitude\"");
    if (!alt)
        return -1;
    return extract_number_after_key(alt, "Quantity", value);
}

/* Procura especificamente o campo de altitude dentro de um bloco de posição. */
static int parse_position_block(char *segment_block, const char *position_key, Position *position)
{
    char pattern[64];
    snprintf(pattern, sizeof(pattern), "\"%s\"", position_key);
    char *p = strstr(segment_block, pattern);
    if (!p)
        return -1;
    char *open = strchr(p, '{');
    if (!open)
        return -1;
    char *close = find_matching_brace(open);
    if (!close)
        return -1;
    char saved = *(close + 1);
    *(close + 1) = '\0';
    int ok = extract_number_after_key(open, "Latitude", &position->latitude) == 0 &&
             extract_number_after_key(open, "Longitude", &position->longitude) == 0 &&
             extract_altitude_quantity(open, &position->altitude_m) == 0;
    *(close + 1) = saved;
    return ok ? 0 : -1;
}

static int extract_profile_number(char *buffer,
                                  const char *section_key,
                                  const char *field_key,
                                  const char *number_key,
                                  double *value)
{
    char section_pattern[64];
    snprintf(section_pattern, sizeof(section_pattern), "\"%s\"", section_key);

    char *section = strstr(buffer, section_pattern);
    if (!section)
        return -1;

    char *field = strstr(section, field_key);
    if (!field)
        return -1;

    return extract_number_after_key(field, number_key, value);
}

/* Analisa um bloco "Start" ou "End" e preenche a estrutura Position. */
int parse_flight_plan(const char *path, FlightPlan *plan, char *err, size_t err_size)
{
    if (!path || !plan)
        return -1;
    memset(plan, 0, sizeof(*plan));
    long size = 0;
    char *buffer = read_file(path, &size);
    if (!buffer)
    {
        snprintf(err, err_size, "Cannot read flight plan '%s'", path);
        return -1;
    }

    /* Buffer contém o JSON inteiro; faço buscas simples por chaves.*/
    double numeric_id = 0;
    if (extract_number_after_key(buffer, "ID", &numeric_id) == 0)
    {
        snprintf(plan->flight_id, sizeof(plan->flight_id), "%.0f", numeric_id);
    }
    else
    {
        snprintf(plan->flight_id, sizeof(plan->flight_id), "%s", path);
    }

    /* Se não houver ID numérico, uso o caminho como identificador. */
    
    plan->climb_speed_knots = 210.0;
    plan->cruise_speed_knots = 460.0;
    plan->descend_speed_knots = 180.0;
    plan->climb_rate_ms = 8.0;
    plan->descend_rate_ms = -7.0;
    plan->fuel_kg = 0.0;

    extract_profile_number(buffer, "Climb", "\"Speed\"", "Value",
                           &plan->climb_speed_knots);
    extract_profile_number(buffer, "Climb", "\"RateClimb\"", "Value",
                           &plan->climb_rate_ms);

    extract_profile_number(buffer, "Cruise", "\"Speed\"", "Value",
                           &plan->cruise_speed_knots);

    extract_profile_number(buffer, "Descend", "\"Speed\"", "Value",
                           &plan->descend_speed_knots);
    extract_profile_number(buffer, "Descend", "\"Ratedescend\"", "Value",
                           &plan->descend_rate_ms);

    char *fuel = strstr(buffer, "\"Fuel\"");
    if (fuel)
    {
        extract_number_after_key(fuel, "Quantity", &plan->fuel_kg);
    }

    char *segments = strstr(buffer, "\"Segments\"");
    if (!segments)
    {
        snprintf(err, err_size, "Flight plan '%s' has no Segments block", path);
        free(buffer);
        return -1;
    }

    char *array_open = strchr(segments, '[');
    if (!array_open)
    {
        snprintf(err, err_size, "Flight plan '%s' has an invalid Segments block", path);
        free(buffer);
        return -1;
    }

    char *cursor = array_open;
    while ((cursor = strchr(cursor, '{')) != NULL && plan->segment_count < MAX_SEGMENTS)
    {
        char *end = find_matching_brace(cursor);
        if (!end)
            break;
        char saved = *(end + 1);
        *(end + 1) = '\0';

        Segment *seg = &plan->segments[plan->segment_count];
        if (extract_string_after_key(cursor, "Mode", seg->mode, sizeof(seg->mode)) == 0 &&
            parse_position_block(cursor, "Start", &seg->start) == 0 &&
            parse_position_block(cursor, "End", &seg->end) == 0)
        {
            plan->segment_count++;
        }

        *(end + 1) = saved;
        cursor = end + 1;
    }

    free(buffer);
    if (plan->segment_count <= 0)
    {
        snprintf(err, err_size, "Flight plan '%s' has no valid segments", path);
        return -1;
    }
    return 0;
}

int parse_departure_time_seconds(const char *path, int *out_seconds,
                                 char *err, size_t err_size)
{
    if (!path || !out_seconds)
        return -1;

    long size = 0;
    char *buffer = read_file(path, &size);
    if (!buffer)
    {
        snprintf(err, err_size, "Cannot read flight plan '%s'", path);
        return -1;
    }

    char value[32];
    if (extract_string_after_key(buffer, "DepartureTime", value, sizeof(value)) != 0)
    {
        snprintf(err, err_size, "Flight plan '%s' has no DepartureTime", path);
        free(buffer);
        return -1;
    }

    int hours = 0;
    int minutes = 0;
    if (sscanf(value, "%d:%d", &hours, &minutes) != 2 ||
        hours < 0 || hours > 23 || minutes < 0 || minutes > 59)
    {
        snprintf(err, err_size,
                 "Flight plan '%s' has invalid DepartureTime '%s'", path, value);
        free(buffer);
        return -1;
    }

    *out_seconds = hours * 3600 + minutes * 60;
    free(buffer);
    return 0;
}
