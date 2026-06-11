package pt.isep.sem4pi.application.service.flightplan;

import eapli.framework.application.ApplicationService;
import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.airport.Coordinates;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.model.flight.*;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanLegDTO;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Arrival;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Date;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Departure;
import pt.isep.sem4pi.domain.model.flight.dep_and_arr.Time;
import pt.isep.sem4pi.domain.model.flight.fuel.Fuel;
import pt.isep.sem4pi.domain.model.flight.fuel.FuelCapacity;
import pt.isep.sem4pi.domain.model.flight.fuel.FuelUnit;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanSegmentDTO;
import pt.isep.sem4pi.domain.model.flight.route.Route;
import pt.isep.sem4pi.domain.model.flight.route.RouteID;
import pt.isep.sem4pi.domain.model.flight.segment.AltitudeSlots;
import pt.isep.sem4pi.domain.model.flight.segment.Segment;
import pt.isep.sem4pi.domain.model.flight.segment.Speed;
import pt.isep.sem4pi.domain.model.flight.segment.WindAngle;
import pt.isep.sem4pi.domain.model.pilot.Pilot;
import pt.isep.sem4pi.domain.model.pilot.PilotId;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.model.route.FlightRouteID;
import pt.isep.sem4pi.domain.model.route.RouteSegment;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;
import pt.isep.sem4pi.domain.repositories.FlightPlanRepository;
import pt.isep.sem4pi.domain.repositories.FlightRepository;
import pt.isep.sem4pi.domain.repositories.FlightRouteRepository;
import pt.isep.sem4pi.domain.repositories.PilotRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@ApplicationService
public class CreateFlightPlanService {

    private final FlightPlanRepository flightPlanRepository;
    private final FlightRouteRepository flightRouteRepository;
    private final AirTransportCompanyRepository companyRepository;
    private final PilotRepository pilotRepository;
    private final FlightRepository flightRepository;

    public CreateFlightPlanService(final FlightPlanRepository flightPlanRepository,
                                   final FlightRouteRepository flightRouteRepository,
                                   final AirTransportCompanyRepository companyRepository,
                                   final PilotRepository pilotRepository,
                                   final FlightRepository flightRepository) {
        this.flightPlanRepository = flightPlanRepository;
        this.flightRouteRepository = flightRouteRepository;
        this.companyRepository = companyRepository;
        this.pilotRepository = pilotRepository;
        this.flightRepository = flightRepository;
    }

    public FlightPlan createFlightPlan(final CreateFlightPlanDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Flight plan data is required.");
        }

        final FlightPlanId planId = resolveFlightPlanId(dto.getFlightPlanId());

        final FlightRoute route = flightRouteRepository.ofIdentity(new FlightRouteID(dto.getRouteId()))
                .orElseThrow(() -> new IllegalArgumentException("Flight route not found."));

        final IataCodeCompany companyCode = route.companyCode();

        final Pilot pilot = pilotRepository.ofIdentity(new PilotId(dto.getPilotId()))
                .orElseThrow(() -> new IllegalArgumentException("Pilot not found."));
        if (!pilot.companyCode().equals(companyCode)) {
            throw new IllegalArgumentException("Pilot must belong to the route's air transport company.");
        }

        final FlightId flightId = new FlightId(dto.getFlightId());
        final Flight flight = flightRepository.ofIdentity(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found."));

        final Aircraft aircraft = findAircraftInCompanyFleet(
                companyCode,
                resolveAircraftRegistration(dto, flight)
        );
        if (aircraft.isRetired()) {
            throw new IllegalArgumentException("Cannot assign a retired aircraft to a flight plan.");
        }

        final FlightType flightType = parseFlightType(dto.getFlightType());
        final List<FlightLeg> legs = buildLegs(dto.getLegs(), route);

        final FlightPlan flightPlan = new FlightPlan(
                planId,
                flightId,
                flightType,
                route,
                legs,
                aircraft,
                pilot
        );

        return flightPlanRepository.save(flightPlan);
    }

    public Iterable<FlightRoute> listRoutes() {
        return flightRouteRepository.findAll();
    }

    public List<Flight> listFlights() {
        final List<Flight> flights = new ArrayList<>();
        flightRepository.findAll().forEach(flights::add);
        return flights;
    }

    public List<FlightPlan> listFlightPlans() {
        final List<FlightPlan> plans = new ArrayList<>();
        flightPlanRepository.findAll().forEach(plans::add);
        return plans;
    }

    private FlightPlanId resolveFlightPlanId(final String requestedId) {
        if (requestedId != null && !requestedId.trim().isEmpty()) {
            final FlightPlanId planId = new FlightPlanId(requestedId.trim());
            if (flightPlanRepository.ofIdentity(planId).isPresent()) {
                throw new IllegalArgumentException("Flight plan id already exists.");
            }
            return planId;
        }
        return generateNextFlightPlanId();
    }

    private FlightPlanId generateNextFlightPlanId() {
        long count = 0;
        for (final FlightPlan ignored : flightPlanRepository.findAll()) {
            count++;
        }
        FlightPlanId candidate = new FlightPlanId(String.format("FP-%06d", count + 1));
        while (flightPlanRepository.ofIdentity(candidate).isPresent()) {
            count++;
            candidate = new FlightPlanId(String.format("FP-%06d", count + 1));
        }
        return candidate;
    }

    public List<Pilot> listPilotsForRoute(final String routeId) {
        final FlightRoute route = flightRouteRepository.ofIdentity(new FlightRouteID(routeId))
                .orElseThrow(() -> new IllegalArgumentException("Flight route not found."));
        return pilotRepository.findByCompany(route.companyCode());
    }

    public List<Aircraft> listAircraftForRoute(final String routeId) {
        final FlightRoute route = flightRouteRepository.ofIdentity(new FlightRouteID(routeId))
                .orElseThrow(() -> new IllegalArgumentException("Flight route not found."));
        return companyRepository.findByIataCode(route.companyCode())
                .map(AirTransportCompany::getFleet)
                .orElse(List.of());
    }

    private Aircraft findAircraftInCompanyFleet(final IataCodeCompany companyCode, final String registration) {
        final AirTransportCompany company = companyRepository.findByIataCode(companyCode)
                .orElseThrow(() -> new IllegalArgumentException("Air transport company not found."));

        final AircraftRegistration aircraftRegistration = new AircraftRegistration(registration);
        return company.getFleet().stream()
                .filter(a -> a.identity().equals(aircraftRegistration))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Aircraft is not part of the route company's fleet."));
    }

    private List<FlightLeg> buildLegs(final List<CreateFlightPlanLegDTO> legDtos, final FlightRoute route) {
        if (legDtos == null || legDtos.isEmpty()) {
            throw new IllegalArgumentException("Flight plan must contain at least one leg.");
        }

        final List<Coordinates> routeWaypoints = routeWaypoints(route);
        final List<FlightLeg> legs = new ArrayList<>();
        int routeLegIndex = 1;
        for (final CreateFlightPlanLegDTO legDto : legDtos) {
            validateLegMatchesRoute(legDto, route, routeLegIndex == 1, routeLegIndex == legDtos.size());

            final Departure departure = new Departure(
                    new IataCodeAirport(legDto.getDepartureAirportIata()),
                    new Date(legDto.getDepartureDay(), legDto.getDepartureMonth(), legDto.getDepartureYear()),
                    new Time(legDto.getDepartureHour(), legDto.getDepartureMinute(), legDto.getDepartureSecond())
            );
            final Arrival arrival = new Arrival(
                    new IataCodeAirport(legDto.getArrivalAirportIata()),
                    new Time(legDto.getArrivalHour(), legDto.getArrivalMinute(), legDto.getArrivalSecond())
            );
            final Fuel fuel = new Fuel(
                    new FuelCapacity(legDto.getFuelQuantity()),
                    parseFuelUnit(legDto.getFuelUnit())
            );
            final Route planRoute = new Route(new RouteID(routeLegIndex), routeWaypoints);
            final List<Segment> segments = buildSegments(legDto.getSegments());

            legs.add(new FlightLeg(departure, arrival, segments, fuel));
            routeLegIndex++;
        }
        return legs;
    }

    private List<Segment> buildSegments(final List<CreateFlightPlanSegmentDTO> segmentDtos) {
        if (segmentDtos == null || segmentDtos.isEmpty()) {
            return List.of();
        }

        final List<Segment> segments = new ArrayList<>();
        for (final CreateFlightPlanSegmentDTO segmentDto : segmentDtos) {
            final List<AltitudeSlots> altitudeSlots = segmentDto.getAltitudeSlots().stream()
                    .map(slot -> new AltitudeSlots(slot.getMinimumAltitude(), slot.getMaximumAltitude()))
                    .toList();
            segments.add(new Segment(
                    new Coordinates(segmentDto.getStartLatitude(), segmentDto.getStartLongitude()),
                    new Coordinates(segmentDto.getEndLatitude(), segmentDto.getEndLongitude()),
                    altitudeSlots,
                    new WindAngle(segmentDto.getWindAngle()),
                    new Speed(segmentDto.getSpeed())
            ));
        }
        return segments;
    }

    private String resolveAircraftRegistration(final CreateFlightPlanDTO dto, final Flight flight) {
        final String fromDto = dto.getAircraftRegistration();
        if (fromDto != null && !fromDto.trim().isEmpty()) {
            final String registration = fromDto.trim();
            if (!registration.equalsIgnoreCase(flight.aircraftRegistration().value())) {
                throw new IllegalArgumentException(
                        "Aircraft registration in file does not match the flight's assigned aircraft."
                );
            }
            return registration;
        }
        return flight.aircraftRegistration().value();
    }

    private List<Coordinates> routeWaypoints(final FlightRoute route) {
        final List<RouteSegment> segments = route.segments();
        if (segments.isEmpty()) {
            throw new IllegalArgumentException("Route has no segments.");
        }
        final List<Coordinates> waypoints = new ArrayList<>();
        waypoints.add(segments.get(0).startNode());
        for (final RouteSegment segment : segments) {
            waypoints.add(segment.endNode());
        }
        return waypoints;
    }

    private void validateLegMatchesRoute(final CreateFlightPlanLegDTO legDto, final FlightRoute route,
                                         final boolean firstLeg, final boolean lastLeg) {
        if (firstLeg && !route.originAirport().code().equalsIgnoreCase(legDto.getDepartureAirportIata())) {
            throw new IllegalArgumentException("First leg departure must match route origin airport.");
        }
        if (lastLeg && !route.destinationAirport().code().equalsIgnoreCase(legDto.getArrivalAirportIata())) {
            throw new IllegalArgumentException("Last leg arrival must match route destination airport.");
        }
    }

    private FlightType parseFlightType(final String rawType) {
        if (rawType == null || rawType.trim().isEmpty()) {
            throw new IllegalArgumentException("Flight type is required.");
        }
        try {
            return FlightType.valueOf(rawType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid flight type. Use REGULAR or CHARTER.");
        }
    }

    private FuelUnit parseFuelUnit(final String rawUnit) {
        if (rawUnit == null || rawUnit.trim().isEmpty()) {
            throw new IllegalArgumentException("Fuel unit is required.");
        }
        try {
            return FuelUnit.valueOf(rawUnit.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid fuel unit.");
        }
    }
}
