#include <assert.h>
#include <stdio.h>
#include <string.h>

#include "../src/simulation.h"

int main(void) {

    // DEFESA:
    // Este teste unitário isola o tracker e prova que um PositionUpdate atualiza estado atual e histórico.
    PositionTracker tracker;
    tracker_init(&tracker);

    PositionUpdate update;

    memset(&update, 0, sizeof(update));

    strcpy(update.flight_id, "TEST-1");
    update.pid = 100;
    update.time_step = 60;

    update.position.latitude = 41.0;
    update.position.longitude = -8.0;
    update.position.altitude_m = 1000.0;

    update.status = STATUS_RUNNING;

    assert(tracker_update(&tracker, &update) == 0);

    const TrackEntry *entry =
            tracker_get_current(&tracker, "TEST-1");

    assert(entry != NULL);

    assert(entry->pid == 100);

    assert(entry->history_count == 1);

    assert(entry->last_time_step == 60);

    tracker_destroy(&tracker);

    printf("test_position_tracker OK\n");

    return 0;
}
