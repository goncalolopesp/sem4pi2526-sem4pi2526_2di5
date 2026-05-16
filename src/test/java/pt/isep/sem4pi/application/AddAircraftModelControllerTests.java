package pt.isep.sem4pi.application;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.model.aircraftModel.MotorizationType;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;

// Importar as tuas classes in-memory reais!
import pt.isep.sem4pi.infrastructure.persistence.InMemoryAircraftModelRepository;
import pt.isep.sem4pi.infrastructure.persistence.InMemoryManufacturerRepository;
import pt.isep.sem4pi.infrastructure.persistence.InMemoryEngineModelRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for the AddAircraftModelController.
 * <p>This class contains unit tests for the AddAircraftModelController,
 * verifying the functionality of adding aircraft models, including validation
 * of uniqueness constraints based on manufacturer and model name.</p>
 */
class AddAircraftModelControllerTests {

    private AddAircraftModelController controller;

    private AircraftModelRepository aircraftRepo;
    private ManufacturerRepository manufacturerRepo;
    private EngineModelRepository engineRepo;

    private Manufacturer dummyManufacturer;
    private List<EngineModelId> dummyEngineIds;

    @BeforeEach
    void setUp() {
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
    void addAircraftModel() {
        // Arrange
        long initialCount = ((InMemoryAircraftModelRepository) aircraftRepo).count();

        // Act
        AircraftModel result = controller.addAircraftModel(
                "B737-ID1", dummyManufacturer, "PASSENGER", "737", MotorizationType.TURBOFAN,
                new BigDecimal("40000"), new BigDecimal("60000"), new BigDecimal("70000"), new BigDecimal("12000"),
                new BigDecimal("12000"), new BigDecimal("800"), new BigDecimal("120"),
                new BigDecimal("0.02"), new BigDecimal("0.5"), new BigDecimal("5000"),
                new BigDecimal("9.5"), dummyEngineIds
        );

        // Assert
        assertNotNull(result);
        assertEquals("B737-ID1", result.identity().toString());
        assertEquals(1, ((InMemoryAircraftModelRepository) aircraftRepo).count() - initialCount);
    }

    @Test
    void ensureCannotAddDuplicateModelNameForSameManufacturer() {
        long initialCount = ((InMemoryAircraftModelRepository) aircraftRepo).count();

        controller.addAircraftModel(
                "A320-ID1", dummyManufacturer, "PASSENGER", "A320", MotorizationType.TURBOFAN,
                new BigDecimal("40000"), new BigDecimal("60000"), new BigDecimal("70000"), new BigDecimal("12000"),
                new BigDecimal("12000"), new BigDecimal("800"), new BigDecimal("120"),
                new BigDecimal("0.02"), new BigDecimal("0.5"), new BigDecimal("5000"),
                new BigDecimal("9.5"), dummyEngineIds
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.addAircraftModel(
                    "A320-ID2", dummyManufacturer, "PASSENGER", "A320", MotorizationType.TURBOFAN,
                    new BigDecimal("40000"), new BigDecimal("70000"), new BigDecimal("80000"), new BigDecimal("12000"),
                    new BigDecimal("12000"), new BigDecimal("800"), new BigDecimal("120"),
                    new BigDecimal("0.02"), new BigDecimal("0.5"), new BigDecimal("5000"),
                    new BigDecimal("9.5"), dummyEngineIds
            );
        });

        assertEquals("This Manufacturer already has an Aircraft Model with the name: A320", exception.getMessage());

        assertEquals(initialCount + 1, ((InMemoryAircraftModelRepository) aircraftRepo).count());
    }
}