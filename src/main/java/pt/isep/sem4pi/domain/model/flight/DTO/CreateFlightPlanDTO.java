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
public class CreateFlightPlanDTO {
    private String flightPlanId;
    private String routeId;
    private String flightId;
    private String flightType;
    private String aircraftRegistration;
    private String pilotId;
    private List<CreateFlightPlanLegDTO> legs;
}
