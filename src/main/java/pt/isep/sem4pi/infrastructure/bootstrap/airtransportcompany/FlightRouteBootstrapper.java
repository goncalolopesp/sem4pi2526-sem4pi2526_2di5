package pt.isep.sem4pi.infrastructure.bootstrap.airtransportcompany;

import pt.isep.sem4pi.application.service.route.CreateFlightRouteService;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.airport.Coordinates;
import pt.isep.sem4pi.domain.model.airport.DTO.CoordinatesDTO;
import pt.isep.sem4pi.domain.model.flight.Flight;
import pt.isep.sem4pi.domain.model.flight.FlightId;
import pt.isep.sem4pi.domain.model.route.AltitudeRange;
import pt.isep.sem4pi.domain.model.route.DTO.AltitudeRangeDTO;
import pt.isep.sem4pi.domain.model.route.DTO.FlightRouteDTO;
import pt.isep.sem4pi.domain.model.route.DTO.RouteSegmentDTO;
import pt.isep.sem4pi.domain.model.route.DTO.WindDTO;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.model.route.FlightRouteID;
import pt.isep.sem4pi.domain.model.route.RouteSegment;
import pt.isep.sem4pi.domain.model.route.Wind;
import pt.isep.sem4pi.domain.repositories.FlightRepository;
import pt.isep.sem4pi.domain.repositories.FlightRouteRepository;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FlightRouteBootstrapper {

    private final CreateFlightRouteService service;
    private final FlightRouteRepository flightRouteRepository;
    private final FlightRepository flightRepository;

    public FlightRouteBootstrapper() {
        final var repositories = PersistenceContext.repositories();
        this.service = new CreateFlightRouteService(repositories.flightRoutes());
        this.flightRouteRepository = repositories.flightRoutes();
        this.flightRepository = repositories.flights();
    }

    public void execute() {
        System.out.println("Bootstrapping Flight Routes...");

        registerSafely("TP1001", "TP", "OPO", "MAD", List.of(
                buildSegment(41.24, -8.67, 40.47, -3.56, 45, 10.0, 850, 30000, 35000, 10.0)
        ));

        registerSafely("AA2002", "AA", "JFK", "LHR", List.of(
                buildSegment(40.64, -73.77, 50.00, -35.00, 270, 25.0, 900, 33000, 37000, 15.0),
                buildSegment(50.00, -35.00, 51.47, -0.45, 260, 20.0, 880, 25000, 30000, 10.0)
        ));

        final FlightId flightId = new FlightId("CONF-101");
        if (!flightRepository.containsOfIdentity(flightId)) {
            final Flight conflictingFlight = new Flight(
                    flightId,
                    new AircraftRegistration("CS-TST"),
                    new FlightRouteID("TP1001"),
                    LocalDate.now().plusMonths(1)
            );
            flightRepository.save(conflictingFlight);
            System.out.println("   -> Bootstrapped conflicting flight CONF-101 for UI testing.");
        }
    }

    private void registerSafely(final String routeId,
                                final String companyCode,
                                final String origin,
                                final String dest,
                                final List<RouteSegmentDTO> segmentDtos) {
        final FlightRouteID identity = new FlightRouteID(routeId);
        final List<RouteSegment> segments = toRouteSegments(segmentDtos);

        final var existingRoute = flightRouteRepository.ofIdentity(identity);
        if (existingRoute.isPresent()) {
            final FlightRoute route = existingRoute.get();
            if (routeNeedsAltitudeRepair(route)) {
                route.replaceSegments(segments);
                flightRouteRepository.save(route);
                System.out.println("   -> Repaired Route altitude slots: " + routeId);
            }
            return;
        }

        try {
            final FlightRouteDTO dto = new FlightRouteDTO(routeId, companyCode, origin, dest, segmentDtos, null);
            service.createFlightRoute(dto);
            System.out.println("   -> Bootstrapped Route: " + routeId);
        } catch (Exception e) {
            System.out.println("   -> Skipped " + routeId + " (Reason: " + e.getMessage() + ")");
        }
    }

    private boolean routeNeedsAltitudeRepair(final FlightRoute route) {
        return route.segments().stream().anyMatch(segment -> segment.allowedAltitudes().isEmpty());
    }

    private List<RouteSegment> toRouteSegments(final List<RouteSegmentDTO> segmentDtos) {
        final List<RouteSegment> segments = new ArrayList<>();
        for (final RouteSegmentDTO segDto : segmentDtos) {
            final Coordinates start = new Coordinates(
                    segDto.getStartNode().getLatitude(),
                    segDto.getStartNode().getLongitude());
            final Coordinates end = new Coordinates(
                    segDto.getEndNode().getLatitude(),
                    segDto.getEndNode().getLongitude());
            final Wind wind = new Wind(segDto.getWind().getDirectionAngle(), segDto.getWind().getSpeed());

            final List<AltitudeRange> altitudes = new ArrayList<>();
            for (final AltitudeRangeDTO altDto : segDto.getAllowedAltitudes()) {
                altitudes.add(new AltitudeRange(
                        altDto.getMinAltitude(),
                        altDto.getMaxAltitude(),
                        altDto.getWidth()));
            }

            segments.add(new RouteSegment(start, end, altitudes, segDto.getMaxSpeed(), wind));
        }
        return segments;
    }

    private RouteSegmentDTO buildSegment(final double startLat, final double startLon,
                                         final double endLat, final double endLon,
                                         final int windDir, final double windSpeed, final int maxSpeed,
                                         final int minAlt, final int maxAlt, final double width) {

        final CoordinatesDTO start = new CoordinatesDTO(startLat, startLon);
        final CoordinatesDTO end = new CoordinatesDTO(endLat, endLon);
        final WindDTO wind = new WindDTO(windDir, windSpeed);
        final List<AltitudeRangeDTO> altitudes = List.of(new AltitudeRangeDTO(minAlt, maxAlt, width));

        return new RouteSegmentDTO(null, start, end, wind, maxSpeed, altitudes);
    }
}
