package pt.isep.sem4pi.application.viadto;

import pt.isep.sem4pi.domain.model.airport.DTO.CoordinatesDTO;
import pt.isep.sem4pi.domain.model.route.DTO.AltitudeRangeDTO;
import pt.isep.sem4pi.domain.model.route.DTO.FlightRouteDTO;
import pt.isep.sem4pi.domain.model.route.DTO.RouteSegmentDTO;
import pt.isep.sem4pi.domain.model.route.DTO.WindDTO;
import eapli.framework.representations.RepresentationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class FlightRouteDTORepresentationBuilder implements RepresentationBuilder<FlightRouteDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlightRouteDTORepresentationBuilder.class);

    private FlightRouteDTO dto = new FlightRouteDTO();
    private String childObject = "";
    private RouteSegmentDTO currentSegmentDTO;
    private AltitudeRangeDTO currentAltitudeDTO;

    @Override
    public FlightRouteDTO build() {
        final FlightRouteDTO ret = dto;
        dto = new FlightRouteDTO();
        return ret;
    }

    @Override
    public RepresentationBuilder<FlightRouteDTO> startObject(final String name) {
        childObject = name;
        if ("segment".equals(name)) {
            currentSegmentDTO = new RouteSegmentDTO();
            currentSegmentDTO.setAllowedAltitudes(new ArrayList<>());
        } else if ("startNode".equals(name) && currentSegmentDTO != null) {
            currentSegmentDTO.setStartNode(new CoordinatesDTO());
        } else if ("endNode".equals(name) && currentSegmentDTO != null) {
            currentSegmentDTO.setEndNode(new CoordinatesDTO());
        } else if ("wind".equals(name) && currentSegmentDTO != null) {
            currentSegmentDTO.setWind(new WindDTO());
        } else if ("altitudeSlot".equals(name)) {
            currentAltitudeDTO = new AltitudeRangeDTO();
        }
        return this;
    }

    @Override
    public RepresentationBuilder<FlightRouteDTO> endObject() {
        if ("segment".equals(childObject) && currentSegmentDTO != null) {
            if (dto.getSegments() == null) dto.setSegments(new ArrayList<>());
            dto.getSegments().add(currentSegmentDTO);
            currentSegmentDTO = null;
            childObject = "FlightRoute";

        } else if ("altitudeSlot".equals(childObject) && currentAltitudeDTO != null && currentSegmentDTO != null) {
            currentSegmentDTO.getAllowedAltitudes().add(currentAltitudeDTO);
            currentAltitudeDTO = null;
            childObject = "segment";

        } else if ("startNode".equals(childObject) || "endNode".equals(childObject) || "wind".equals(childObject)) {
            childObject = "segment";
        } else {
            childObject = "";
        }
        return this;
    }

    @Override
    public RepresentationBuilder<FlightRouteDTO> withProperty(final String name, final String value) {
        if ("FlightRoute".equals(childObject)) {
            if ("routeID".equals(name)) dto.setRouteID(value);
            else if ("companyCode".equals(name)) dto.setCompanyCode(value);
            else if ("originAirport".equals(name)) dto.setOriginAirport(value);
            else if ("destinationAirport".equals(name)) dto.setDestinationAirport(value);
        }
        return this;
    }

    @Override
    public RepresentationBuilder<FlightRouteDTO> withProperty(final String name, final Double value) {
        if ("startNode".equals(childObject) && currentSegmentDTO != null) {
            if ("latitude".equals(name)) currentSegmentDTO.getStartNode().setLatitude(value);
            else if ("longitude".equals(name)) currentSegmentDTO.getStartNode().setLongitude(value);

        } else if ("endNode".equals(childObject) && currentSegmentDTO != null) {
            if ("latitude".equals(name)) currentSegmentDTO.getEndNode().setLatitude(value);
            else if ("longitude".equals(name)) currentSegmentDTO.getEndNode().setLongitude(value);

        } else if ("wind".equals(childObject) && currentSegmentDTO != null) {
            if ("speed".equals(name)) currentSegmentDTO.getWind().setSpeed(value);

        } else if ("altitudeSlot".equals(childObject) && currentAltitudeDTO != null) {
            if ("width".equals(name)) currentAltitudeDTO.setWidth(value);
        }
        return this;
    }

    @Override
    public RepresentationBuilder<FlightRouteDTO> withProperty(final String name, final Integer value) {
        if ("segment".equals(childObject) && currentSegmentDTO != null) {
            if ("maxSpeed".equals(name)) currentSegmentDTO.setMaxSpeed(value);

        } else if ("wind".equals(childObject) && currentSegmentDTO != null) {
            if ("directionAngle".equals(name)) currentSegmentDTO.getWind().setDirectionAngle(value);

        } else if ("altitudeSlot".equals(childObject) && currentAltitudeDTO != null) {
            if ("minAltitude".equals(name)) currentAltitudeDTO.setMinAltitude(value);
            else if ("maxAltitude".equals(name)) currentAltitudeDTO.setMaxAltitude(value);
        }
        return this;
    }

    @Override
    public RepresentationBuilder<FlightRouteDTO> withProperty(final String name, final Long value) {
        if ("FlightRoute".equals(childObject) && "version".equals(name)) {
            dto.setVersion(value);
        } else if ("segment".equals(childObject) && "segmentId".equals(name) && currentSegmentDTO != null) {
            currentSegmentDTO.setSegmentId(value);
        }
        return this;
    }

    @Override
    public RepresentationBuilder<FlightRouteDTO> startCollection(String name) {
        if ("segments".equals(name) && dto.getSegments() == null) {
            dto.setSegments(new ArrayList<>());
        }
        return this;
    }

    @Override
    public RepresentationBuilder<FlightRouteDTO> endCollection() {
        return this;
    }

    @Override
    public RepresentationBuilder<FlightRouteDTO> withElement(String value) {
        LOGGER.warn("FlightRouteDTO não usa coleções de elementos simples. Elemento ignorado: {}", value);
        return this;
    }
}