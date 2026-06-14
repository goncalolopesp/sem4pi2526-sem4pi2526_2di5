package pt.isep.sem4pi.domain.model.simulation.DTO;

import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationDTO {

    private String simulationId;

    private String airControlAreaId;

    private String startTime;

    private String endTime;

    private Integer timeStep;

    private Double safetyThreshold;

    private String status;

    private List<String> flightPlanIds;

    private Long version;
}