#include <assert.h>
#include <stdio.h>
#include <string.h>

#include "../src/simulation.h"

int main(void) {

    PositionTracker tracker;
    tracker_init(&tracker);

    SimulationConfig config;

    memset(&config,0,sizeof(config));

    config.safety_min_sep_nm=3.0;
    config.safety_min_alt_m=300;

    PositionUpdate update;

    memset(&update,0,sizeof(update));

    strcpy(update.flight_id,"LOW-ALT");

    update.pid=1;

    update.time_step=0;

    update.position.latitude=41;
    update.position.longitude=-8;
    update.position.altitude_m=100;

    update.status=STATUS_RUNNING;

    tracker_update(&tracker,&update);

    SafetyLog log;

    safety_log_init(&log);

    FlightRuntime runtimes[1];

    memset(runtimes,0,sizeof(runtimes));

    strcpy(runtimes[0].flight_id,"LOW-ALT");

    runtimes[0].pid=1;
    runtimes[0].active=1;

    int violations=
            safety_check_step(
                    &tracker,
                    &config,
                    0,
                    &log,
                    runtimes,
                    1);

    assert(violations>0);

    assert(log.count==1);

    tracker_destroy(&tracker);

    printf("test_safety_monitor OK\n");

    return 0;
}