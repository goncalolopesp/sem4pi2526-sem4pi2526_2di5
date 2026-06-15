package pt.isep.sem4pi.domain.model.airport.DTO;

import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatesDTO {
    private Double latitude;
    private Double longitude;
}