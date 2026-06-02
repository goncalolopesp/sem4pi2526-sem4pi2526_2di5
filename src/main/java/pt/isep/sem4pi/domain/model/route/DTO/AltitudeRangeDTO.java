package pt.isep.sem4pi.domain.model.route.DTO;

import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AltitudeRangeDTO {
    private Integer minAltitude;
    private Integer maxAltitude;
    private Double width;
}