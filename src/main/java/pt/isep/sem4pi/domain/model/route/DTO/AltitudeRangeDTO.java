 package pt.isep.sem4pi.domain.model.route.DTO;

import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for AltitudeRange.
 *
 * This DTO is used to transfer altitude range information between layers
 * without exposing the domain model directly.
 *
 * @author SEM4PI Team
 */
@DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AltitudeRangeDTO {
    /** The minimum altitude in feet */
    private Integer minAltitude;

    /** The maximum altitude in feet */
    private Integer maxAltitude;

    /** The width of the altitude slot */
    private Double width;
}