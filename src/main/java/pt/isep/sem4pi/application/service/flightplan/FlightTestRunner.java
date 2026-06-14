package pt.isep.sem4pi.application.service.flightplan;

public interface FlightTestRunner {

    FlightTestExecutionResult run(String jsonPlanPath,
                                  double minAltitudeMeters,
                                  double minReserveFuelKg,
                                  int stepSeconds,
                                  String reportPath);
}
