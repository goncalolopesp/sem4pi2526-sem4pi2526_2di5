package pt.isep.sem4pi.domain.model.route.DTO;

import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightRouteDTO {
    private String routeID;
    private String companyCode;
    private String originAirport;
    private String destinationAirport;
    private List<RouteSegmentDTO> segments;
    private Long version;
}