package pt.isep.sem4pi.domain.model.route.DTO;

import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import pt.isep.sem4pi.domain.model.airport.DTO.CoordinatesDTO;

import java.util.List;

/**
 * Data Transfer Object for RouteSegment.
 *
 * This DTO is used to transfer route segment information between layers
 * without exposing the domain model directly. It includes all information
 * needed to define a segment of a flight route.
 *
 * @author SEM4PI Team
 */
@DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteSegmentDTO {
    /** The unique identifier of the segment */
    private Long segmentId;

    /** The starting point coordinates */
    private CoordinatesDTO startNode;

    /** The ending point coordinates */
    private CoordinatesDTO endNode;

    /** The wind conditions for this segment */
    private WindDTO wind;

    /** The maximum speed allowed for this segment */
    private Integer maxSpeed;

    /** The list of allowed altitude ranges for this segment */
    private List<AltitudeRangeDTO> allowedAltitudes;
}