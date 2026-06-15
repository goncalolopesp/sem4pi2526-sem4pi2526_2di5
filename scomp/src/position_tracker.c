#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "simulation.h"

void tracker_init(PositionTracker *tracker)
{
    memset(tracker, 0, sizeof(*tracker));
}

void tracker_destroy(PositionTracker *tracker)
{
    for (int i = 0; i < tracker->entry_count; ++i)
    {
        free(tracker->entries[i].history);
    }
    memset(tracker, 0, sizeof(*tracker));
}

static int find_entry(PositionTracker *tracker, const char *flight_id)
{
    for (int i = 0; i < tracker->entry_count; ++i)
    {
        if (strcmp(tracker->entries[i].flight_id, flight_id) == 0)
        {
            return i;
        }
    }
    return -1;
}

static int create_entry(PositionTracker *tracker, const char *flight_id, pid_t pid)
{
    if (tracker->entry_count >= MAX_TRACKED_FLIGHTS)
    {
        fprintf(stderr, "tracker: max tracked flights (%d) reached\n", MAX_TRACKED_FLIGHTS);
        return -1;
    }
    TrackEntry *entry = &tracker->entries[tracker->entry_count];
    memset(entry, 0, sizeof(*entry));
    snprintf(entry->flight_id, sizeof(entry->flight_id), "%s", flight_id);
    entry->pid = pid;
    return tracker->entry_count++;
}

static int append_to_history(TrackEntry *entry, const TrackPoint *point)
{
    if (entry->history_count == entry->history_capacity)
    {
        size_t new_cap = entry->history_capacity == 0 ? 16 : entry->history_capacity * 2;
        TrackPoint *new_buf = realloc(entry->history, new_cap * sizeof(*new_buf));
        if (!new_buf)
        {
            perror("tracker: realloc history");
            return -1;
        }
        entry->history = new_buf;
        entry->history_capacity = new_cap;
    }
    entry->history[entry->history_count++] = *point;
    return 0;
}

int tracker_update(PositionTracker *tracker, const PositionUpdate *update)
{
    // DEFESA:
    // O tracker concentra estado atual e histórico, mantendo alta coesão na responsabilidade de acompanhamento.
    int idx = find_entry(tracker, update->flight_id);
    if (idx < 0)
    {
        idx = create_entry(tracker, update->flight_id, update->pid);
        if (idx < 0)
            return -1;
    }

    TrackEntry *entry = &tracker->entries[idx];

    entry->current_position = update->position;
    entry->last_time_step = update->time_step;
    entry->status = update->status;

    TrackPoint point;
    point.time_step = update->time_step;
    point.position = update->position;
    point.status = update->status;

    return append_to_history(entry, &point);
}

const TrackEntry *tracker_get_current(const PositionTracker *tracker, const char *flight_id)
{
    for (int i = 0; i < tracker->entry_count; ++i)
    {
        if (strcmp(tracker->entries[i].flight_id, flight_id) == 0)
        {
            return &tracker->entries[i];
        }
    }
    return NULL;
}

void tracker_print_summary(const PositionTracker *tracker)
{
    printf("\n=== Position Tracker Summary (%d flights) ===\n", tracker->entry_count);
    for (int i = 0; i < tracker->entry_count; ++i)
    {
        const TrackEntry *e = &tracker->entries[i];
        const char *status_str =
            e->status == STATUS_RUNNING ? "RUNNING" : e->status == STATUS_FINISHED ? "FINISHED"
                                                  : e->status == STATUS_ABORTED    ? "ABORTED"
                                                                                   : "UNKNOWN";
        printf("  [%s] pid=%d last_t=%d pos=(%.6f, %.6f, %.1fm) status=%s history=%zu pts\n",
               e->flight_id, e->pid, e->last_time_step,
               e->current_position.latitude,
               e->current_position.longitude,
               e->current_position.altitude_m,
               status_str,
               e->history_count);
    }
    printf("=============================================\n\n");
}
