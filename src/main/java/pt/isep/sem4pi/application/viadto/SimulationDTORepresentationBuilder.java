package pt.isep.sem4pi.application.viadto;

import eapli.framework.representations.RepresentationBuilder;
import pt.isep.sem4pi.domain.model.simulation.DTO.SimulationDTO;

import java.util.ArrayList;

public class SimulationDTORepresentationBuilder
        implements RepresentationBuilder<SimulationDTO> {

    private SimulationDTO dto = new SimulationDTO();

    private String currentObject = "";

    @Override
    public SimulationDTO build() {
        final SimulationDTO ret = dto;
        dto = new SimulationDTO();
        return ret;
    }

    @Override
    public RepresentationBuilder<SimulationDTO> startObject(String name) {
        currentObject = name;
        return this;
    }

    @Override
    public RepresentationBuilder<SimulationDTO> endObject() {
        currentObject = "";
        return this;
    }

    @Override
    public RepresentationBuilder<SimulationDTO> withProperty(String name, String value) {

        if ("Simulation".equals(currentObject)) {

            if ("simulationId".equals(name))
                dto.setSimulationId(value);

            else if ("airControlAreaId".equals(name))
                dto.setAirControlAreaId(value);

            else if ("startTime".equals(name))
                dto.setStartTime(value);

            else if ("endTime".equals(name))
                dto.setEndTime(value);

            else if ("status".equals(name))
                dto.setStatus(value);
        }

        return this;
    }

    @Override
    public RepresentationBuilder<SimulationDTO> withProperty(String name, Integer value) {

        if ("Simulation".equals(currentObject)
                && "timeStep".equals(name)) {

            dto.setTimeStep(value);
        }

        return this;
    }

    @Override
    public RepresentationBuilder<SimulationDTO> withProperty(String name, Double value) {

        if ("Simulation".equals(currentObject)
                && "safetyThreshold".equals(name)) {

            dto.setSafetyThreshold(value);
        }

        return this;
    }

    @Override
    public RepresentationBuilder<SimulationDTO> withProperty(String name, Long value) {

        if ("Simulation".equals(currentObject)
                && "version".equals(name)) {

            dto.setVersion(value);
        }

        return this;
    }

    @Override
    public RepresentationBuilder<SimulationDTO> startCollection(String name) {

        if ("flightPlanIds".equals(name)) {
            dto.setFlightPlanIds(new ArrayList<>());
        }

        return this;
    }

    @Override
    public RepresentationBuilder<SimulationDTO> endCollection() {
        return this;
    }

    @Override
    public RepresentationBuilder<SimulationDTO> withElement(String value) {

        if (dto.getFlightPlanIds() != null) {
            dto.getFlightPlanIds().add(value);
        }

        return this;
    }
}