package pt.isep.sem4pi.application.service.aircraftModel;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.engineModel.FuelType;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AddAircraftModelServiceTest {

    private AuthorizationService authzMock;
    private AircraftModelRepository repoMock;
    private AddAircraftModelService service;

    private Manufacturer dummyManufacturer;
    private List<EngineModel> dummyEngines;

    @Before
    public void setUp() {
        authzMock = mock(AuthorizationService.class);
        repoMock = mock(AircraftModelRepository.class);

        // Create the new mock
        EngineModelRepository engineRepoMock = mock(EngineModelRepository.class);

        dummyManufacturer = new Manufacturer(new ManufacturerId("AIRBUS"), "Airbus", "France");

        EngineModel dummyEngine = new EngineModel(
                new EngineModelId("CFM56"), dummyManufacturer, "CFM56",
                EngineType.TURBOFAN, new BigDecimal("10000"), FuelType.JET_A1, new BigDecimal("0.80")
        );
        dummyEngines = new ArrayList<>();
        dummyEngines.add(dummyEngine);

        // Tell the mock to return the dummy engine when asked
        when(engineRepoMock.ofIdentity(any())).thenReturn(java.util.Optional.of(dummyEngine));

        // Add to constructor
        service = new AddAircraftModelService(authzMock, repoMock, engineRepoMock);
    }

    @Test
    public void ensureAircraftModelIsCreatedSuccessfully() {
        when(repoMock.containsOfIdentity(any(AircraftModelId.class))).thenReturn(false);

        AircraftModel result = service.addAircraftModel(
                dummyManufacturer, "PASSENGER", "A320", EngineType.TURBOFAN,
                new BigDecimal("40000"), new BigDecimal("60000"), new BigDecimal("70000"), // Pesos válidos
                new BigDecimal("20000"), new BigDecimal("39000"), new BigDecimal("800"),
                new BigDecimal("120"), new BigDecimal("0.5"), new BigDecimal("1.5"),       // Drag e Lift válidos
                new BigDecimal("5000"), new BigDecimal("9.5"), dummyEngines, 180           // Lotação 180
        );

        assertNotNull(result);
        assertEquals("AIRBUS", result.identity().manufacturerId().toString());
        assertEquals("A320", result.identity().modelName());

        verify(authzMock, times(1)).ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.BACKOFFICE_OPERATOR, SEM4PIRoles.ADMIN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCannotAddDuplicateAircraftModel() {
        when(repoMock.containsOfIdentity(any(AircraftModelId.class))).thenReturn(true);

        service.addAircraftModel(
                dummyManufacturer, "PASSENGER", "A320", EngineType.TURBOFAN,
                new BigDecimal("40000"), new BigDecimal("60000"), new BigDecimal("70000"),
                new BigDecimal("20000"), new BigDecimal("39000"), new BigDecimal("800"),
                new BigDecimal("120"), new BigDecimal("0.5"), new BigDecimal("1.5"),
                new BigDecimal("5000"), new BigDecimal("9.5"), dummyEngines, 180
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCannotAddWithInvalidAircraftTypeString() {
        when(repoMock.containsOfIdentity(any(AircraftModelId.class))).thenReturn(false);

        service.addAircraftModel(
                dummyManufacturer, "SPACESHIP", "A320", EngineType.TURBOFAN,
                new BigDecimal("40000"), new BigDecimal("60000"), new BigDecimal("70000"),
                new BigDecimal("20000"), new BigDecimal("39000"), new BigDecimal("800"),
                new BigDecimal("120"), new BigDecimal("0.5"), new BigDecimal("1.5"),
                new BigDecimal("5000"), new BigDecimal("9.5"), dummyEngines, 180
        );
    }
}