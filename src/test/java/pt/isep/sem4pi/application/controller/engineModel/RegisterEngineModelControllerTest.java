package pt.isep.sem4pi.application.controller.engineModel;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.engineModel.FuelType;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class RegisterEngineModelControllerTest {

    private String testManCode;

    @Before
    public void setUp() {
        System.setProperty("persistence.repositoryFactory", InMemoryRepositoryFactory.class.getName());

        final var factory = PersistenceContext.repositories();
        final var userRepo = factory.users();

        AuthzRegistry.configure(userRepo, new SEM4PIPasswordPolicy(), new PlainTextEncoder());

        SystemUserBuilder userBuilder =
                new SystemUserBuilder(new SEM4PIPasswordPolicy(), new PlainTextEncoder());

        if (!userRepo.containsOfIdentity(eapli.framework.infrastructure.authz.domain.model.Username.valueOf("admin"))) {
            userBuilder.withUsername("admin").withPassword("Password1").withName("Admin", "User").withEmail("admin@isep.pt").withRoles(pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles.ADMIN);
            userRepo.save(userBuilder.build());
        }

        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }

        final var manufacturers = factory.manufacturers();
        this.testManCode = "M-" + java.util.UUID.randomUUID().toString().substring(0, 4);
        Manufacturer m = new Manufacturer(new ManufacturerId(testManCode), "Test Man", "France");
        manufacturers.save(m);
    }

    @Test
    public void registerEngineModelCreatesEngineModel() {
        AuthzRegistry.authenticationService().authenticate("admin", "Password1");

        final RegisterEngineModelController controller = new RegisterEngineModelController();
        String uniqueCode = "ENG-" + java.util.UUID.randomUUID().toString().substring(0, 6);

        final var engineModel = controller.registerEngineModel(
                uniqueCode,
                this.testManCode,
                uniqueCode,
                EngineType.TURBOFAN,
                new BigDecimal("120.00"),
                FuelType.JET_A1,
                new BigDecimal("37.50")
        );

        assertNotNull(engineModel);
        assertTrue(PersistenceContext.repositories().engineModels().containsOfIdentity(new EngineModelId(uniqueCode)));
    }

    @Test
    public void listManufacturersReturnsSeededManufacturer() {
        AuthzRegistry.authenticationService().authenticate("admin", "Password1");

        final RegisterEngineModelController controller = new RegisterEngineModelController();
        final var manufacturers = controller.listManufacturers();

        assertFalse(manufacturers.isEmpty());
        boolean found = manufacturers.stream().anyMatch(m -> m.identity().toString().equals(this.testManCode));
        assertTrue(found);
    }

    @Test
    public void registerEngineModelRejectsUnauthenticatedUser() {
        final RegisterEngineModelController controller = new RegisterEngineModelController();

        try {
            controller.registerEngineModel(
                    "ENG-REJECT",
                    this.testManCode,
                    "ENG-REJECT",
                    EngineType.TURBOFAN,
                    new BigDecimal("120.00"),
                    FuelType.JET_A1,
                    new BigDecimal("37.50")
            );
            fail("Expected an exception for unauthenticated user");
        } catch (RuntimeException ex) {
            assertTrue(true);
        }
    }
}
