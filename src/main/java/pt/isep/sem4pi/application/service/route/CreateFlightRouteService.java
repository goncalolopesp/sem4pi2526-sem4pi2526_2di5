package pt.isep.sem4pi.application.service.route;

import eapli.framework.application.ApplicationService;
import pt.isep.sem4pi.domain.model.airport.Coordinates;
import pt.isep.sem4pi.domain.model.airport.IataCodeAirport;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;
import pt.isep.sem4pi.domain.model.route.*;
import pt.isep.sem4pi.domain.route.*;
import pt.isep.sem4pi.domain.model.route.DTO.FlightRouteDTO;
import pt.isep.sem4pi.domain.repositories.FlightRouteRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for orchestrating the creation of a Flight Route.
 * It transforms the incoming DTO into Domain Objects and persists the aggregate.
 */
@ApplicationService
public class CreateFlightRouteService {

    private final FlightRouteRepository routeRepository;

    public CreateFlightRouteService(FlightRouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

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