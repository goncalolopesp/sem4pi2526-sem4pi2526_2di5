package pt.isep.sem4pi.application.controller.aircontrolarea;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlArea;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlAreaID;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.domain.usermanagement.UserBuilderHelper;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAirControlAreaRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;

import static org.junit.Assert.*;

public class RegisterAirControlAreaControllerTest {

    private InMemoryAirControlAreaRepository repository;
    private RegisterAirControlAreaController controller;

    @Before
    public void setUp() {
        System.setProperty("persistence.repositoryFactory", InMemoryRepositoryFactory.class.getName());
        PersistenceContext.resetForTesting();
        final var factory = PersistenceContext.repositories();
        AuthzRegistry.configure(factory.users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());

        final var userRepo = factory.users();
        if (!userRepo.ofIdentity(Username.valueOf("admin")).isPresent()) {
            userRepo.save(UserBuilderHelper.builder()
                    .withUsername("admin")
                    .withPassword("Password1")
                    .withName("Admin", "User")
                    .withEmail("admin@example.org")
                    .withRoles(SEM4PIRoles.ADMIN)
                    .build());
        }

        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }

        repository = new InMemoryAirControlAreaRepository();

        controller = new RegisterAirControlAreaController(repository, AuthzRegistry.authorizationService());
    }

    @Test
    public void ensureControllerSavesNewAirControlAreaSuccessfully() {
        AuthzRegistry.authenticationService().authenticate("admin", "Password1");

        AirControlArea area = controller.registerAirControlArea(
                "LPPC", 36.0, 42.0, -10.0, -6.0, "Lisbon ACC", 1500.0
        );

        assertNotNull("A Área de Controlo Aéreo não deve ser nula.", area);
        assertTrue("A Área devia ter sido guardada no repositório.", repository.containsOfIdentity(new AirControlAreaID("LPPC")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureControllerPreventsDuplicateAirControlAreas() {
        AuthzRegistry.authenticationService().authenticate("admin", "Password1");

        controller.registerAirControlArea("MAD_FIR", 35.0, 43.0, -5.0, 0.0, "Madrid FIR", 1200.0);

        controller.registerAirControlArea("MAD_FIR", 36.0, 44.0, -6.0, -1.0, "Madrid ACC", 1000.0);
    }

    @Test(expected = Exception.class)
    public void ensureControllerRejectsUnauthenticatedUser() {
        controller.registerAirControlArea(
                "UNAUTH_FIR", 35.0, 43.0, -5.0, 0.0, "Unauth FIR", 1200.0
        );
    }
}
