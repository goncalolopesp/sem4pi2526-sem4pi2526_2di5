package pt.isep.sem4pi.application.controller.aircraftmodel;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.application.controller.aircraftModel.AddAircraftModelController;
import pt.isep.sem4pi.application.service.aircraftModel.AddAircraftModelService;
import pt.isep.sem4pi.domain.model.aircraftModel.DTO.AircraftModelDTO;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.engineModel.FuelType;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAircraftModelRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryManufacturerRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryEngineModelRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AddAircraftModelControllerTests {

    private AddAircraftModelController controller;
    private AircraftModelRepository aircraftRepo;
    private ManufacturerRepository manufacturerRepo;
    private EngineModelRepository engineRepo;
    private AddAircraftModelService addAircraftModelService;
    private Manufacturer dummyManufacturer;
    private List<EngineModel> dummyEngineModels;

    @Before
    public void setUp() {
        aircraftRepo = new InMemoryAircraftModelRepository();
        manufacturerRepo = new InMemoryManufacturerRepository();
        engineRepo = new InMemoryEngineModelRepository();

        AuthorizationService authzMock = mock(AuthorizationService.class);

        addAircraftModelService = new AddAircraftModelService(authzMock, aircraftRepo, engineRepo);

        controller = new AddAircraftModelController(
                authzMock, aircraftRepo, manufacturerRepo, engineRepo, addAircraftModelService);

        dummyManufacturer = mock(Manufacturer.class);
        when(dummyManufacturer.identity()).thenReturn(new ManufacturerId("AIRBUS"));

        dummyEngineModels = new ArrayList<>();
        EngineModelId engineId = new EngineModelId("CFM56");
        EngineModel engine = new EngineModel(engineId, dummyManufacturer, "CFM56",
                EngineType.TURBOFAN, new BigDecimal("10000"),
                FuelType.JET_A1, new BigDecimal("0.80"));
        dummyEngineModels.add(engine);

        engineRepo.save(engine);
    }

    @Test
    public void addAircraftModel() {
        long initialCount = ((InMemoryAircraftModelRepository) aircraftRepo).count();

        AircraftModelDTO result = controller.addAircraftModel(
                dummyManufacturer, "PASSENGER", "B737", EngineType.TURBOFAN,
                new BigDecimal("40000"), new BigDecimal("60000"), new BigDecimal("70000"),
                new BigDecimal("12000"), new BigDecimal("12000"), new BigDecimal("800"),
                new BigDecimal("120"), new BigDecimal("0.02"), new BigDecimal("0.5"),
                new BigDecimal("5000"), new BigDecimal("9.5"), dummyEngineModels, 180
        );

        assertNotNull(result);

        assertEquals("AIRBUS", result.getManufacturerId());
        assertEquals("B737", result.getModelName());

        assertEquals(1, ((InMemoryAircraftModelRepository) aircraftRepo).count() - initialCount);
    }

    @Test
    public void ensureCannotAddDuplicateModelNameForSameManufacturer() {
        long initialCount = ((InMemoryAircraftModelRepository) aircraftRepo).count();

        controller.addAircraftModel(
                dummyManufacturer, "PASSENGER", "A320", EngineType.TURBOFAN,
                new BigDecimal("40000"), new BigDecimal("60000"), new BigDecimal("70000"),
                new BigDecimal("12000"), new BigDecimal("12000"), new BigDecimal("800"),
                new BigDecimal("120"), new BigDecimal("0.02"), new BigDecimal("0.5"),
                new BigDecimal("5000"), new BigDecimal("9.5"), dummyEngineModels, 180
        );

        try {
            controller.addAircraftModel(
                    dummyManufacturer, "PASSENGER", "A320", EngineType.TURBOFAN,
                    new BigDecimal("40000"), new BigDecimal("70000"), new BigDecimal("80000"),
                    new BigDecimal("12000"), new BigDecimal("12000"), new BigDecimal("800"),
                    new BigDecimal("120"), new BigDecimal("0.02"), new BigDecimal("0.5"),
                    new BigDecimal("5000"), new BigDecimal("9.5"), dummyEngineModels, 180
            );
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException exception) {
            assertEquals("Already exists a model with this name for this manufacturer.",
                    exception.getMessage());
        }

        assertEquals(initialCount + 1, ((InMemoryAircraftModelRepository) aircraftRepo).count());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureCannotAddAircraftModelWithoutAnyEngine() {
        controller.addAircraftModel(
                dummyManufacturer, "PASSENGER", "B747", EngineType.TURBOFAN,
                new BigDecimal("40000"), new BigDecimal("60000"), new BigDecimal("70000"),
                new BigDecimal("12000"), new BigDecimal("12000"), new BigDecimal("800"),
                new BigDecimal("120"), new BigDecimal("0.02"), new BigDecimal("0.5"),
                new BigDecimal("5000"), new BigDecimal("9.5"), Collections.emptyList(), 180
        );
    }

    @Test
    public void ensureDefaultConstructorWorks() {
        System.setProperty("persistence.repositoryFactory",
                "pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory");

        AuthzRegistry.configure(
                PersistenceContext.repositories().users(),
                new SEM4PIPasswordPolicy(),
                new PlainTextEncoder()
        );

        AddAircraftModelController defaultController = new AddAircraftModelController();

        assertNotNull(defaultController);
    }

    @Test
    public void ensureGetManufacturersReturnsData() {
        Manufacturer realMan = new Manufacturer(new ManufacturerId("BOEING"), "Boeing", "USA");
        manufacturerRepo.save(realMan);

        Iterable<Manufacturer> result = controller.getManufacturers();
        List<Manufacturer> list = new ArrayList<>();
        result.forEach(list::add);

        assertFalse(list.isEmpty());
        assertEquals("BOEING", list.get(0).identity().toString());
    }

    @Test
    public void ensureGetEngineModelsReturnsData() {
        Iterable<EngineModel> result = controller.getEngineModels();
        List<EngineModel> list = new ArrayList<>();
        result.forEach(list::add);

        assertFalse(list.isEmpty());
        assertEquals("CFM56", list.get(0).identity().toString());
    }
}