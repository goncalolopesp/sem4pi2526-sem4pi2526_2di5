package pt.isep.sem4pi.domain.model.aircraftModel.DTO;

import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WingGeometryDTO {
    private Double wingArea;
    private Double aspectRatio;
}