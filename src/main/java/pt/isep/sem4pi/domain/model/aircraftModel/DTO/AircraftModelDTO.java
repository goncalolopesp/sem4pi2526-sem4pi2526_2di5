package pt.isep.sem4pi.domain.model.aircraftModel.DTO;

import eapli.framework.representations.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AircraftModelDTO {

    public static final double NO_INFO = -1;

    private String manufacturerId;
    private String modelName;

    private String aircraftType;
    private String motorization;
    private Integer maximumCapacity;

    private FuelDTO fuel;
    private WingGeometryDTO wingGeometry;
    private AircraftPerformanceDTO performance;
    private WeightDTO weightLimits;
    private CoefficientDTO aerodynamics;

    private List<CertifiedEngineDTO> certifiedEngines;

    private Long version;
}