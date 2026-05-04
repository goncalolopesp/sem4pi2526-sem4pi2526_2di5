package pt.isep.sem4pi.application;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.sem4pi.domain.model.Manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.Manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModel;
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

class AddAircraftModelControllerTests {

    private AddAircraftModelController controller;

    private AircraftModelRepository aircraftRepo;
    private ManufacturerRepository manufacturerRepo;
    private EngineModelRepository engineRepo;

    private Manufacturer dummyManufacturer;
    private List<EngineModelId> dummyEngineIds;

    @BeforeEach
    void setUp() {
        // 1. Inicializar as implementações Em Memória fornecidas/criadas na EAPLI
        aircraftRepo = new InMemoryAircraftModelRepository();
        manufacturerRepo = new InMemoryManufacturerRepository();
        engineRepo = new InMemoryEngineModelRepository();

        // Fazemos mock apenas da autorização para não termos de fazer login simulado
        AuthorizationService authzMock = mock(AuthorizationService.class);

        // 2. Injetar os repositórios in-memory no Controller
        controller = new AddAircraftModelController(authzMock, aircraftRepo, manufacturerRepo, engineRepo);

        // Preparar dados comuns
        dummyManufacturer = mock(Manufacturer.class);
        when(dummyManufacturer.identity()).thenReturn(new ManufacturerId("AIRBUS"));
        dummyEngineIds = new ArrayList<>();
        dummyEngineIds.add(new EngineModelId("CFM56"));
    }

    @Test
    void ensureCannotAddDuplicateModelNameForSameManufacturer() {
        // Arrange: Em vez de mandarmos o mock fingir, vamos GRAVAR DE VERDADE
        // um avião no repositório em memória para poluir a "base de dados"
        controller.addAircraftModel(
                "A320-ID1", dummyManufacturer, "PASSENGER", "A320", "TURBOFAN",
                new BigDecimal("40000"), new BigDecimal("70000"), new BigDecimal("20000"),
                new BigDecimal("12000"), new BigDecimal("800"), new BigDecimal("120"),
                new BigDecimal("0.02"), new BigDecimal("0.5"), new BigDecimal("5000"),
                new BigDecimal("9.5"), dummyEngineIds
        );

        // Act & Assert: Tentar gravar um SEGUNDO avião com o mesmo Manufacturer e Nome
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            controller.addAircraftModel(
                    "A320-ID2", dummyManufacturer, "PASSENGER", "A320", "TURBOFAN", // <- Mesmo nome "A320"
                    new BigDecimal("40000"), new BigDecimal("70000"), new BigDecimal("20000"),
                    new BigDecimal("12000"), new BigDecimal("800"), new BigDecimal("120"),
                    new BigDecimal("0.02"), new BigDecimal("0.5"), new BigDecimal("5000"),
                    new BigDecimal("9.5"), dummyEngineIds
            );
        });

        assertEquals("This Manufacturer already has an Aircraft Model with the name: A320", exception.getMessage());

        // Verificação extra: Garantir que a BD em memória só tem 1 avião guardado e não 2
        assertEquals(1, ((InMemoryAircraftModelRepository) aircraftRepo).count());
    }
}