package pt.isep.sem4pi.application.service.route;

import eapli.framework.application.ApplicationService;
import pt.isep.sem4pi.domain.model.airport.Coordinates;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.model.route.*;
import pt.isep.sem4pi.domain.model.route.DTO.FlightRouteDTO;
import pt.isep.sem4pi.domain.repositories.FlightRouteRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application service for creating new flight routes.
 *
 * This service orchestrates the creation of a new flight route by transforming
 * the incoming DTO into domain objects and persisting them using the repository.
 * It handles the business logic of converting DTOs to domain entities and ensures
 * they are properly validated and persisted.
 *
 * @author SEM4PI Team
 */
@ApplicationService
public class CreateFlightRouteService {

    /** Repository for flight route persistence */
    private final FlightRouteRepository routeRepository;

    /**
     * Constructs a CreateFlightRouteService with the specified repository.
     *
     * @param routeRepository the repository used to persist flight routes
     */
    public CreateFlightRouteService(FlightRouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    /**
     * Creates a new flight route from the provided DTO.
     *
     * Converts the DTO's data into domain objects (RouteSegments with coordinates,
     * wind, and altitude info), creates a new FlightRoute aggregate, and persists it
     * through the repository.
     *
     * @param dto the data transfer object containing the route information
     * @return the created and persisted FlightRoute
     *
     * @throws IllegalArgumentException if the DTO data is invalid (e.g., invalid route ID format,
     *                                   mismatched company codes, invalid coordinates)
     */
    public FlightRoute createFlightRoute(FlightRouteDTO dto) {

        List<RouteSegment> segments = dto.getSegments().stream().map(segDto -> {
            Coordinates start = new Coordinates(segDto.getStartNode().getLatitude(), segDto.getStartNode().getLongitude());
            Coordinates end = new Coordinates(segDto.getEndNode().getLatitude(), segDto.getEndNode().getLongitude());
            Wind wind = new Wind(segDto.getWind().getDirectionAngle(), segDto.getWind().getSpeed());

            List<AltitudeRange> altitudes = segDto.getAllowedAltitudes().stream()
                    .map(alt -> new AltitudeRange(alt.getMinAltitude(), alt.getMaxAltitude(), alt.getWidth()))
                    .collect(Collectors.toList());

            return new RouteSegment(start, end, altitudes, segDto.getMaxSpeed(), wind);
        }).collect(Collectors.toList());

        FlightRoute newRoute = new FlightRoute(
                new FlightRouteID(dto.getRouteID()),
                new IataCodeCompany(dto.getCompanyCode()),
                new IataCodeAirport(dto.getOriginAirport()),
                new IataCodeAirport(dto.getDestinationAirport()),
                segments
        );

        return routeRepository.save(newRoute);
    }
}