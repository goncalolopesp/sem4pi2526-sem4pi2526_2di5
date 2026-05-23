package pt.isep.sem4pi.domain.model.aircraftModel.DTO;

import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoefficientDTO {
    private Double dragCoefficient;
    private Double liftCoefficient;
}