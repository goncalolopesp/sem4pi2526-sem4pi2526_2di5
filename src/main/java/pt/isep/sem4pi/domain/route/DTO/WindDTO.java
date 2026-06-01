package pt.isep.sem4pi.domain.route.DTO;

import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WindDTO {
    private Integer directionAngle;
    private Double speed;
}