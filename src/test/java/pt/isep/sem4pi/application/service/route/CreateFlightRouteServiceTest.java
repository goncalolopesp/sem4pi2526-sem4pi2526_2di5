package pt.isep.sem4pi.application.service.route;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.isep.sem4pi.domain.model.airport.DTO.CoordinatesDTO;
import pt.isep.sem4pi.domain.model.route.DTO.AltitudeRangeDTO;
import pt.isep.sem4pi.domain.model.route.DTO.FlightRouteDTO;
import pt.isep.sem4pi.domain.model.route.DTO.RouteSegmentDTO;
import pt.isep.sem4pi.domain.model.route.DTO.WindDTO;
import pt.isep.sem4pi.domain.repositories.FlightRouteRepository;
import pt.isep.sem4pi.domain.model.route.FlightRoute;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CreateFlightRouteServiceTest {

    private FlightRouteRepository mockRepo;
    private CreateFlightRouteService service;

    @Before
    public void setUp() {
        mockRepo = Mockito.mock(FlightRouteRepository.class);
        service = new CreateFlightRouteService(mockRepo);
    }

    private FlightRouteDTO buildValidDTO() {
        AltitudeRangeDTO altDTO = new AltitudeRangeDTO(1000, 5000, 100.0);
        WindDTO windDTO = new WindDTO(180, 15.0);
        CoordinatesDTO startNode = new CoordinatesDTO(41.15, -8.61);
        CoordinatesDTO endNode = new CoordinatesDTO(38.77, -9.13);
        RouteSegmentDTO segDTO = new RouteSegmentDTO(null, startNode, endNode, windDTO, 500,
                Arrays.asList(altDTO));

        return new FlightRouteDTO("TP12", "TP", "OPO", "LIS",
                Arrays.asList(segDTO), null);
    }

    // --- Happy path ---

    @Test
    public void ensureCreateFlightRouteReturnsFlightRoute() {
        FlightRouteDTO dto = buildValidDTO();

        when(mockRepo.save(any(FlightRoute.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlightRoute result = service.createFlightRoute(dto);
        assertNotNull(result);
    }

    @Test
    public void ensureCreateFlightRouteCallsSaveOnRepository() {
        FlightRouteDTO dto = buildValidDTO();

        when(mockRepo.save(any(FlightRoute.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createFlightRoute(dto);
        verify(mockRepo, times(1)).save(any(FlightRoute.class));
    }

    @Test
    public void ensureCreatedRouteHasCorrectIdentity() {
        FlightRouteDTO dto = buildValidDTO();

        when(mockRepo.save(any(FlightRoute.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlightRoute result = service.createFlightRoute(dto);
        assertEquals("TP12", result.identity().code());
    }

    // --- Multiple segments ---

    @Test
    public void ensureCreateFlightRouteWithMultipleSegments() {
        AltitudeRangeDTO altDTO = new AltitudeRangeDTO(2000, 8000, 200.0);
        WindDTO windDTO = new WindDTO(90, 10.0);
        CoordinatesDTO start1 = new CoordinatesDTO(41.15, -8.61);
        CoordinatesDTO end1 = new CoordinatesDTO(40.0, -8.0);
        CoordinatesDTO start2 = new CoordinatesDTO(40.0, -8.0);
        CoordinatesDTO end2 = new CoordinatesDTO(38.77, -9.13);

        RouteSegmentDTO seg1 = new RouteSegmentDTO(null, start1, end1, windDTO, 500, Arrays.asList(altDTO));
        RouteSegmentDTO seg2 = new RouteSegmentDTO(null, start2, end2, windDTO, 600, Arrays.asList(altDTO));

        FlightRouteDTO dto = new FlightRouteDTO("TP12", "TP", "OPO", "LIS",
                Arrays.asList(seg1, seg2), null);

        when(mockRepo.save(any(FlightRoute.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlightRoute result = service.createFlightRoute(dto);
        assertNotNull(result);
    }

    // --- Invalid domain data should propagate exceptions ---

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateFlightRouteFailsWithInvalidRouteID() {
        FlightRouteDTO dto = buildValidDTO();
        dto.setRouteID("INVALID_ID!");

        service.createFlightRoute(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateFlightRouteFailsWithMismatchedCompanyCode() {
        FlightRouteDTO dto = buildValidDTO();
        dto.setRouteID("AB12");
        dto.setCompanyCode("TP"); // AB != TP

        service.createFlightRoute(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateFlightRouteFailsWithInvalidCoordinates() {
        AltitudeRangeDTO altDTO = new AltitudeRangeDTO(1000, 5000, 100.0);
        WindDTO windDTO = new WindDTO(180, 15.0);
        CoordinatesDTO startNode = new CoordinatesDTO(999.0, -8.61); // invalid latitude
        CoordinatesDTO endNode = new CoordinatesDTO(38.77, -9.13);
        RouteSegmentDTO segDTO = new RouteSegmentDTO(null, startNode, endNode, windDTO, 500,
                Arrays.asList(altDTO));

        FlightRouteDTO dto = new FlightRouteDTO("TP12", "TP", "OPO", "LIS",
                Arrays.asList(segDTO), null);

        service.createFlightRoute(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateFlightRouteFailsWithInvalidWind() {
        AltitudeRangeDTO altDTO = new AltitudeRangeDTO(1000, 5000, 100.0);
        WindDTO windDTO = new WindDTO(400, 15.0); // invalid direction
        CoordinatesDTO startNode = new CoordinatesDTO(41.15, -8.61);
        CoordinatesDTO endNode = new CoordinatesDTO(38.77, -9.13);
        RouteSegmentDTO segDTO = new RouteSegmentDTO(null, startNode, endNode, windDTO, 500,
                Arrays.asList(altDTO));

        FlightRouteDTO dto = new FlightRouteDTO("TP12", "TP", "OPO", "LIS",
                Arrays.asList(segDTO), null);

        service.createFlightRoute(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateFlightRouteFailsWithInvalidAltitude() {
        AltitudeRangeDTO altDTO = new AltitudeRangeDTO(-1, 5000, 100.0); // negative min
        WindDTO windDTO = new WindDTO(180, 15.0);
        CoordinatesDTO startNode = new CoordinatesDTO(41.15, -8.61);
        CoordinatesDTO endNode = new CoordinatesDTO(38.77, -9.13);
        RouteSegmentDTO segDTO = new RouteSegmentDTO(null, startNode, endNode, windDTO, 500,
                Arrays.asList(altDTO));

        FlightRouteDTO dto = new FlightRouteDTO("TP12", "TP", "OPO", "LIS",
                Arrays.asList(segDTO), null);

        service.createFlightRoute(dto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCreateFlightRouteFailsWithZeroMaxSpeed() {
        AltitudeRangeDTO altDTO = new AltitudeRangeDTO(1000, 5000, 100.0);
        WindDTO windDTO = new WindDTO(180, 15.0);
        CoordinatesDTO startNode = new CoordinatesDTO(41.15, -8.61);
        CoordinatesDTO endNode = new CoordinatesDTO(38.77, -9.13);
        RouteSegmentDTO segDTO = new RouteSegmentDTO(null, startNode, endNode, windDTO, 0, // zero
                Arrays.asList(altDTO));

        FlightRouteDTO dto = new FlightRouteDTO("TP12", "TP", "OPO", "LIS",
                Arrays.asList(segDTO), null);

        service.createFlightRoute(dto);
    }

    // --- Repository failure propagation ---

    @Test(expected = RuntimeException.class)
    public void ensureRepositoryExceptionIsPropagated() {
        FlightRouteDTO dto = buildValidDTO();

        when(mockRepo.save(any(FlightRoute.class))).thenThrow(new RuntimeException("DB error"));

        service.createFlightRoute(dto);
    }
}
