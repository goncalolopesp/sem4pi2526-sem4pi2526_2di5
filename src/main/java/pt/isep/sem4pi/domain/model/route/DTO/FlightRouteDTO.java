package pt.isep.sem4pi.domain.model.route.DTO;

import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Data Transfer Object for FlightRoute.
 *
 * This DTO is used to transfer flight route information between layers
 * without exposing the domain model directly. It encapsulates all information
 * needed to create or retrieve a complete flight route.
 *
 * @author SEM4PI Team
 */
@DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightRouteDTO {
    /** The unique identifier of the route */
    private String routeID;

    /** The IATA code of the airline company */
    private String companyCode;

    /** The IATA code of the origin airport */
    private String originAirport;

    /** The IATA code of the destination airport */
    private String destinationAirport;

    /** The list of segments that compose this route */
    private List<RouteSegmentDTO> segments;

    /** The version field for optimistic locking */
    private Long version;
}