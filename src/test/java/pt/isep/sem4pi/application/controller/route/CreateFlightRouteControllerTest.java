package pt.isep.sem4pi.application.controller.route;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import pt.isep.sem4pi.application.service.route.CreateFlightRouteService;
import pt.isep.sem4pi.domain.model.airport.DTO.CoordinatesDTO;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airport.Airport;
import pt.isep.sem4pi.domain.model.route.DTO.AltitudeRangeDTO;
import pt.isep.sem4pi.domain.model.route.DTO.FlightRouteDTO;
import pt.isep.sem4pi.domain.model.route.DTO.RouteSegmentDTO;
import pt.isep.sem4pi.domain.model.route.DTO.WindDTO;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;
import pt.isep.sem4pi.domain.repositories.AirportRepository;
import pt.isep.sem4pi.domain.model.route.FlightRoute;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CreateFlightRouteControllerTest {

    private AuthorizationService mockAuthz;
    private CreateFlightRouteService mockService;
    private AirTransportCompanyRepository mockCompanyRepo;
    private AirportRepository mockAirportRepo;
    private CreateFlightRouteController controller;

    @Before
    public void setUp() {
        mockAuthz = Mockito.mock(AuthorizationService.class);
        mockService = Mockito.mock(CreateFlightRouteService.class);
        mockCompanyRepo = Mockito.mock(AirTransportCompanyRepository.class);
        mockAirportRepo = Mockito.mock(AirportRepository.class);

        controller = new CreateFlightRouteController(mockAuthz, mockService, mockCompanyRepo, mockAirportRepo);
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

    // --- createFlightRoute ---

    @Test
    public void ensureCreateFlightRouteChecksAuthorization() {
        FlightRouteDTO dto = buildValidDTO();
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        when(mockService.createFlightRoute(dto)).thenReturn(mockRoute);

        controller.createFlightRoute(dto);

        verify(mockAuthz, times(1)).ensureAuthenticatedUserHasAnyOf(any());
    }

    @Test
    public void ensureCreateFlightRouteDelegatesToService() {
        FlightRouteDTO dto = buildValidDTO();
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        when(mockService.createFlightRoute(dto)).thenReturn(mockRoute);

        FlightRoute result = controller.createFlightRoute(dto);

        verify(mockService, times(1)).createFlightRoute(dto);
        assertEquals(mockRoute, result);
    }

    @Test
    public void ensureCreateFlightRouteReturnsFlightRoute() {
        FlightRouteDTO dto = buildValidDTO();
        FlightRoute mockRoute = Mockito.mock(FlightRoute.class);
        when(mockService.createFlightRoute(dto)).thenReturn(mockRoute);

        FlightRoute result = controller.createFlightRoute(dto);
        assertNotNull(result);
    }

    // --- getCompanies ---

    @Test
    public void ensureGetCompaniesChecksAuthorization() {
        when(mockCompanyRepo.findAll()).thenReturn(Collections.emptyList());

        controller.getCompanies();

        verify(mockAuthz, times(1)).ensureAuthenticatedUserHasAnyOf(any());
    }

    @Test
    public void ensureGetCompaniesDelegatesToRepository() {
        Iterable<AirTransportCompany> mockCompanies = Collections.emptyList();
        when(mockCompanyRepo.findAll()).thenReturn(mockCompanies);

        Iterable<AirTransportCompany> result = controller.getCompanies();

        verify(mockCompanyRepo, times(1)).findAll();
        assertEquals(mockCompanies, result);
    }

    // --- getAirports ---

    @Test
    public void ensureGetAirportsChecksAuthorization() {
        when(mockAirportRepo.findAll()).thenReturn(Collections.emptyList());

        controller.getAirports();

        verify(mockAuthz, times(1)).ensureAuthenticatedUserHasAnyOf(any());
    }

    @Test
    public void ensureGetAirportsDelegatesToRepository() {
        Iterable<Airport> mockAirports = Collections.emptyList();
        when(mockAirportRepo.findAll()).thenReturn(mockAirports);

        Iterable<Airport> result = controller.getAirports();

        verify(mockAirportRepo, times(1)).findAll();
        assertEquals(mockAirports, result);
    }

    // --- Authorization failure ---

    @Test(expected = Exception.class)
    public void ensureCreateFlightRouteFailsWhenNotAuthorized() {
        doThrow(new RuntimeException("Not authorized"))
                .when(mockAuthz).ensureAuthenticatedUserHasAnyOf(any());

        controller.createFlightRoute(buildValidDTO());
    }

    @Test(expected = Exception.class)
    public void ensureGetCompaniesFailsWhenNotAuthorized() {
        doThrow(new RuntimeException("Not authorized"))
                .when(mockAuthz).ensureAuthenticatedUserHasAnyOf(any());

        controller.getCompanies();
    }

    @Test(expected = Exception.class)
    public void ensureGetAirportsFailsWhenNotAuthorized() {
        doThrow(new RuntimeException("Not authorized"))
                .when(mockAuthz).ensureAuthenticatedUserHasAnyOf(any());

        controller.getAirports();
    }

    // --- Service exception propagation ---

    @Test(expected = RuntimeException.class)
    public void ensureServiceExceptionIsPropagated() {
        FlightRouteDTO dto = buildValidDTO();
        when(mockService.createFlightRoute(dto)).thenThrow(new RuntimeException("Service error"));

        controller.createFlightRoute(dto);
    }
}
