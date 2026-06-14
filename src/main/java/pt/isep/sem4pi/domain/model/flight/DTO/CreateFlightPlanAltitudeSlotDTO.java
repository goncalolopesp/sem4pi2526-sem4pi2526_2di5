package pt.isep.sem4pi.domain.model.flight.DTO;

import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFlightPlanAltitudeSlotDTO {
    private Integer minimumAltitude;
    private Integer maximumAltitude;
}
