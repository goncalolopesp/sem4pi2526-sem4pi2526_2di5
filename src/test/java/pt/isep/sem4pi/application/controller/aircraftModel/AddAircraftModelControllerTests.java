package pt.isep.sem4pi.application.controller.aircraftModel;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.application.AddAircraftModelController;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.model.aircraftModel.MotorizationType;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;

import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAircraftModelRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryManufacturerRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryEngineModelRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AddAircraftModelControllerTests {

    private AddAircraftModelController controller;
    private AircraftModelRepository aircraftRepo;
    private ManufacturerRepository manufacturerRepo;
    private EngineModelRepository engineRepo;
    private Manufacturer dummyManufacturer;
    private List<EngineModelId> dummyEngineIds;

    @Before
    public void setUp() {
        aircraftRepo = new InMemoryAircraftModelRepository();
        manufacturerRepo = new InMemoryManufacturerRepository();
        engineRepo = new InMemoryEngineModelRepository();

        AuthorizationService authzMock = mock(AuthorizationService.class);

        controller = new AddAircraftModelController(authzMock, aircraftRepo, manufacturerRepo, engineRepo);

        dummyManufacturer = mock(Manufacturer.class);
        when(dummyManufacturer.identity()).thenReturn(new ManufacturerId("AIRBUS"));

        dummyEngineIds = new ArrayList<>();
        dummyEngineIds.add(new EngineModelId("CFM56"));
    }

    @Test
    public void addAircraftModel() {
        long initialCount = ((InMemoryAircraftModelRepository) aircraftRepo).count();

        AircraftModel result = controller.addAircraftModel(
                dummyManufacturer, "PASSENGER", "B737", MotorizationType.TURBOFAN,
                new BigDecimal("40000"), new BigDecimal("60000"), new BigDecimal("70000"), new BigDecimal("12000"),
                new BigDecimal("12000"), new BigDecimal("800"), new BigDecimal("120"),
                new BigDecimal("0.02"), new BigDecimal("0.5"), new BigDecimal("5000"),
                new BigDecimal("9.5"), dummyEngineIds
        );

        assertNotNull(result);

        AircraftModelId expectedId = new AircraftModelId(new ManufacturerId("AIRBUS"), "B737");
        assertEquals(expectedId, result.identity());

        assertEquals(1, ((InMemoryAircraftModelRepository) aircraftRepo).count() - initialCount);
    }

    @Test
    public void ensureCannotAddDuplicateModelNameForSameManufacturer() {
        long initialCount = ((InMemoryAircraftModelRepository) aircraftRepo).count();

        controller.addAircraftModel(
                dummyManufacturer, "PASSENGER", "A320", MotorizationType.TURBOFAN,
                new BigDecimal("40000"), new BigDecimal("60000"), new BigDecimal("70000"), new BigDecimal("12000"),
                new BigDecimal("12000"), new BigDecimal("800"), new BigDecimal("120"),
                new BigDecimal("0.02"), new BigDecimal("0.5"), new BigDecimal("5000"),
                new BigDecimal("9.5"), dummyEngineIds
        );

        try {
            controller.addAircraftModel(
                    dummyManufacturer, "PASSENGER", "A320", MotorizationType.TURBOFAN,
                    new BigDecimal("40000"), new BigDecimal("70000"), new BigDecimal("80000"), new BigDecimal("12000"),
                    new BigDecimal("12000"), new BigDecimal("800"), new BigDecimal("120"),
                    new BigDecimal("0.02"), new BigDecimal("0.5"), new BigDecimal("5000"),
                    new BigDecimal("9.5"), dummyEngineIds
            );
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            assertEquals("Already exists a model with this name for this manufacturer.", exception.getMessage());
        }

        assertEquals(initialCount + 1, ((InMemoryAircraftModelRepository) aircraftRepo).count());
    }
}