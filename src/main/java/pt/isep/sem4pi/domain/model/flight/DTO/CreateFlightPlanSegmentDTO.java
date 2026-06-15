package pt.isep.sem4pi.domain.model.flight.DTO;

import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFlightPlanSegmentDTO {
    private Double startLatitude;
    private Double startLongitude;
    private Double endLatitude;
    private Double endLongitude;
    private List<CreateFlightPlanAltitudeSlotDTO> altitudeSlots;
    private Double windAngle;
    private Double speed;
}
