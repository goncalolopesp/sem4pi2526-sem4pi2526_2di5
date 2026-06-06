package pt.isep.sem4pi.domain.model.flight.DTO;

import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFlightPlanLegDTO {
    private String departureAirportIata;
    private Integer departureDay;
    private Integer departureMonth;
    private Integer departureYear;
    private Integer departureHour;
    private Integer departureMinute;
    private Integer departureSecond;
    private String arrivalAirportIata;
    private Integer arrivalHour;
    private Integer arrivalMinute;
    private Integer arrivalSecond;
    private Double fuelQuantity;
    private String fuelUnit;
}
