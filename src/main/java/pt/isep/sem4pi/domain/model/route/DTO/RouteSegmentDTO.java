package pt.isep.sem4pi.domain.model.route.DTO;

import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import pt.isep.sem4pi.domain.model.airport.DTO.CoordinatesDTO;

import java.util.List;

@DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteSegmentDTO {
    private Long segmentId;
    private CoordinatesDTO startNode;
    private CoordinatesDTO endNode;
    private WindDTO wind;
    private Integer maxSpeed;
    private List<AltitudeRangeDTO> allowedAltitudes;
}