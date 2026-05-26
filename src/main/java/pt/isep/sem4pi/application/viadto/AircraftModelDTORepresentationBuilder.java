package pt.isep.sem4pi.application.viadto;

import pt.isep.sem4pi.domain.model.aircraftModel.DTO.*;
import eapli.framework.representations.RepresentationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;

public class AircraftModelDTORepresentationBuilder implements RepresentationBuilder<AircraftModelDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AircraftModelDTORepresentationBuilder.class);

    private AircraftModelDTO dto = new AircraftModelDTO();
    private String childObject = "";
    private CertifiedEngineDTO currentEngineDTO;

    @Override
    public AircraftModelDTO build() {
        final AircraftModelDTO ret = dto;
        dto = new AircraftModelDTO(); // Reset for reuse
        return ret;
    }

    @Override
    public RepresentationBuilder<AircraftModelDTO> startObject(final String name) {
        childObject = name;
        if ("certifiedEngine".equals(name)) {
            currentEngineDTO = new CertifiedEngineDTO();
        }
        return this;
    }

    @Override
    public RepresentationBuilder<AircraftModelDTO> endObject() {
        if ("certifiedEngine".equals(childObject) && currentEngineDTO != null) {
            dto.getCertifiedEngines().add(currentEngineDTO);
            currentEngineDTO = null;
        }
        childObject = "";
        return this;
    }

    @Override
    public RepresentationBuilder<AircraftModelDTO> withProperty(final String name, final String value) {
        if ("AircraftModel".equals(childObject)) {
            if ("manufacturerId".equals(name)) dto.setManufacturerId(value);
            else if ("modelName".equals(name)) dto.setModelName(value);
            else if ("aircraftType".equals(name)) dto.setAircraftType(value);
            else if ("motorization".equals(name)) dto.setMotorization(value);

        } else if ("certifiedEngine".equals(childObject) && "engineModelId".equals(name)) {
            currentEngineDTO.setEngineModelId(value);
        }
        return this;
    }

    @Override
    public RepresentationBuilder<AircraftModelDTO> withProperty(final String name, final Double value) {

        if ("weightLimits".equals(childObject)) {
            if (dto.getWeightLimits() == null) dto.setWeightLimits(new WeightDTO());

            if ("emptyWeight".equals(name)) dto.getWeightLimits().setEmptyWeight(value);
            else if ("maxZeroFuelWeight".equals(name)) dto.getWeightLimits().setMaxZeroFuelWeight(value);
            else if ("maxTakeOffWeight".equals(name)) dto.getWeightLimits().setMaxTakeOffWeight(value);
        }
        else if ("aerodynamics".equals(childObject)) {
            if (dto.getAerodynamics() == null) dto.setAerodynamics(new CoefficientDTO());

            if ("dragCoefficient".equals(name)) dto.getAerodynamics().setDragCoefficient(value);
            else if ("liftCoefficient".equals(name)) dto.getAerodynamics().setLiftCoefficient(value);
        }
        else if ("fuel".equals(childObject)) {
            if (dto.getFuel() == null) dto.setFuel(new FuelDTO());

            if ("maximumFuelCapacity".equals(name)) dto.getFuel().setMaxFuelCapacity(value);
        }
        else if ("wingGeometry".equals(childObject)) {
            if (dto.getWingGeometry() == null) dto.setWingGeometry(new WingGeometryDTO());

            if ("wingArea".equals(name)) dto.getWingGeometry().setWingArea(value);
            else if ("aspectRatio".equals(name)) dto.getWingGeometry().setAspectRatio(value);
        }
        else if ("performance".equals(childObject)) {
            if (dto.getPerformance() == null) dto.setPerformance(new AircraftPerformanceDTO());

            if ("serviceCeiling".equals(name)) dto.getPerformance().setServiceCeiling(value);
            else if ("cruiseSpeed".equals(name)) dto.getPerformance().setCruiseSpeed(value);
            else if ("maxRangeKm".equals(name)) dto.getPerformance().setMaximumRangeKm(value);
        }

        return this;
    }

    @Override
    public RepresentationBuilder<AircraftModelDTO> withProperty(final String name, final Long value) {
        if ("AircraftModel".equals(childObject) && "version".equals(name)) {
            dto.setVersion(value);
        }
        return this;
    }

    @Override
    public RepresentationBuilder<AircraftModelDTO> withProperty(final String name, final Integer value) {
        if ("AircraftModel".equals(childObject) && "maximumCapacity".equals(name)) {
            dto.setMaximumCapacity(value);
        }
        return this;
    }

    @Override
    public RepresentationBuilder<AircraftModelDTO> startCollection(String name) {
        if ("certifiedEngines".equals(name) && dto.getCertifiedEngines() == null) {
            dto.setCertifiedEngines(new ArrayList<>());
        }
        return this;
    }

    @Override
    public RepresentationBuilder<AircraftModelDTO> endCollection() {
        return this;
    }

    @Override
    public RepresentationBuilder<AircraftModelDTO> withElement(String value) {
        LOGGER.warn("AircraftModelDTO não usa coleções de elementos simples. Elemento ignorado: {}", value);
        return this;
    }
}