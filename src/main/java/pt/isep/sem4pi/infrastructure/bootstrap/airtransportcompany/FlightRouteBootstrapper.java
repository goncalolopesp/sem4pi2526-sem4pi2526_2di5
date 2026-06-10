package pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany;

import pt.isep.sem4pi.application.service.route.CreateFlightRouteService;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.airport.DTO.CoordinatesDTO;
import pt.isep.sem4pi.domain.model.flight.Flight;
import pt.isep.sem4pi.domain.model.flight.FlightId;
import pt.isep.sem4pi.domain.model.route.DTO.AltitudeRangeDTO;
import pt.isep.sem4pi.domain.model.route.DTO.FlightRouteDTO;
import pt.isep.sem4pi.domain.model.route.DTO.RouteSegmentDTO;
import pt.isep.sem4pi.domain.model.route.DTO.WindDTO;
import pt.isep.sem4pi.domain.model.route.FlightRouteID;
import pt.isep.sem4pi.domain.repositories.FlightRepository;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.List;

public class FlightRouteBootstrapper {

    private final CreateFlightRouteService service;
    private final FlightRepository flightRepository;

    public FlightRouteBootstrapper() {
        // Use Service directly to avoid error coming from authentication
        this.service = new CreateFlightRouteService(PersistenceContext.repositories().flightRoutes());
        this.flightRepository = PersistenceContext.repositories().flights();
    }

    public void execute() {
        System.out.println("Bootstrapping Flight Routes...");

        registerSafely("TP1001", "TP", "OPO", "MAD", List.of(
                buildSegment(41.24, -8.67, 40.47, -3.56, 45, 10.0, 850, 30000, 35000, 10.0)
        ));

        registerSafely("AA2002", "AA", "JFK", "LHR", List.of(
                buildSegment(40.64, -73.77, 50.00, -35.00, 270, 25.0, 900, 33000, 37000, 15.0), // Saída para o Oceano
                buildSegment(50.00, -35.00, 51.47, -0.45, 260, 20.0, 880, 25000, 30000, 10.0)   // Aproximação à Europa
        ));

        // Creates a flight for route TP1001 scheduled for 1 month from now
        FlightId flightId = new FlightId("CONF-101");
        if (!flightRepository.containsOfIdentity(flightId)) {
            Flight conflictingFlight = new Flight(
                    flightId,
                    new AircraftRegistration("CS-TST"),
                    new FlightRouteID("TP1001"),
                    LocalDate.now().plusMonths(1)
            );
            flightRepository.save(conflictingFlight);
            System.out.println("   -> Bootstrapped conflicting flight CONF-101 for UI testing.");
        }
    }

    private void registerSafely(String routeId, String companyCode, String origin, String dest, List<RouteSegmentDTO> segments) {
        try {
            FlightRouteDTO dto = new FlightRouteDTO(routeId, companyCode, origin, dest, segments, null);
            service.createFlightRoute(dto);
            System.out.println("   -> Bootstrapped Route: " + routeId);
        } catch (Exception e) {
            System.out.println("   -> Skipped " + routeId + " (Reason: " + e.getMessage() + ")");
        }
    }

    private RouteSegmentDTO buildSegment(double startLat, double startLon, double endLat, double endLon,
                                         int windDir, double windSpeed, int maxSpeed,
                                         int minAlt, int maxAlt, double width) {

        CoordinatesDTO start = new CoordinatesDTO(startLat, startLon);
        CoordinatesDTO end = new CoordinatesDTO(endLat, endLon);
        WindDTO wind = new WindDTO(windDir, windSpeed);
        List<AltitudeRangeDTO> altitudes = List.of(new AltitudeRangeDTO(minAlt, maxAlt, width));

        return new RouteSegmentDTO(null, start, end, wind, maxSpeed, altitudes);
    }
}
