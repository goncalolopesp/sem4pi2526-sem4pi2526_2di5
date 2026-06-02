package pt.isep.sem4pi.application.controller.engineModel;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.engineModel.EngineType;
import pt.isep.sem4pi.domain.model.engineModel.FuelType;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;

import java.math.BigDecimal;

import static org.junit.Assert.*;

public class RegisterEngineModelControllerTest {

    @Before
    public void setUp() {
        System.setProperty("persistence.repositoryFactory", InMemoryRepositoryFactory.class.getName());

        final var factory = PersistenceContext.repositories();
        AuthzRegistry.configure(factory.users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());

        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }

        final var manufacturers = PersistenceContext.repositories().manufacturers();
        manufacturers.save(new Manufacturer(new ManufacturerId("CFM"), "CFM International", "France"));
    }

    @Test
    public void listManufacturersReturnsSeededManufacturer() {
        AuthzRegistry.authenticationService().authenticate("admin", "Password1");

        final RegisterEngineModelController controller = new RegisterEngineModelController();
        final var manufacturers = controller.listManufacturers();

        assertFalse(manufacturers.isEmpty());
        boolean containsCFM = false;
        for (Manufacturer m : manufacturers) {
            if (m.identity().toString().equals("CFM")) {
                containsCFM = true;
                break;
            }
        }

        assertTrue("The list should contain the seeded CFM manufacturer", containsCFM);
    }

    @Test
    public void registerEngineModelRejectsUnauthenticatedUser() {
        final RegisterEngineModelController controller = new RegisterEngineModelController();

        try {
            controller.registerEngineModel(
                    "CFM56-5B",
                    "CFM",
                    "CFM56-5B",
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
