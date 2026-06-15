package pt.isep.sem4pi.domain.model.route.DTO;

import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Wind.
 *
 * This DTO is used to transfer wind condition information between layers
 * without exposing the domain model directly.
 *
 * @author SEM4PI Team
 */
@DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WindDTO {
    /** The wind direction in degrees relative to North (0-359) */
    private Integer directionAngle;

    /** The wind speed in meters per second */
    private Double speed;
}