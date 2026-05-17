package pt.isep.sem4pi.application.controller.airtransportcompany;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompanyCollaborator;
import pt.isep.sem4pi.domain.model.airtransportcompany.CompanyName;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCode;
import pt.isep.sem4pi.domain.model.airtransportcompany.IcaoCode;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAirTransportCompanyCollaboratorRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAirTransportCompanyRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;

import static org.junit.Assert.*;

public class RegisterCustomerCollaboratorControllerTest {

    private InMemoryAirTransportCompanyRepository companyRepo;
    private InMemoryAirTransportCompanyCollaboratorRepository collaboratorRepo;
    private RegisterCustomerCollaboratorController controller;

    private static final IcaoCode TAP_ICAO = new IcaoCode("TAP");

    @Before
    public void setUp() {
        System.setProperty("persistence.repositoryFactory", InMemoryRepositoryFactory.class.getName());

        final var factory = PersistenceContext.repositories();
        AuthzRegistry.configure(factory.users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());

        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }

        companyRepo = new InMemoryAirTransportCompanyRepository();
        collaboratorRepo = new InMemoryAirTransportCompanyCollaboratorRepository();
        controller = new RegisterCustomerCollaboratorController(companyRepo, collaboratorRepo);

        companyRepo.save(new AirTransportCompany(
                new CompanyName("TAP Portugal"),
                new IataCode("TP"),
                TAP_ICAO
        ));
    }

    @Test
    public void registerCompanyCollaborator_whenAuthenticatedAndCompanyExists_savesCollaborator() {
        AuthzRegistry.authenticationService().authenticate("admin", "Password1");

        final String username = "atcc" + System.nanoTime();
        final AirTransportCompanyCollaborator result = controller.registerCompanyCollaborator(
                username,
                "Password1",
                "John",
                "Doe",
                username + "@example.org",
                TAP_ICAO
        );

        assertNotNull(result);
        assertEquals(TAP_ICAO, result.company().identityIcao());
        assertEquals(username, result.systemUser().identity().toString());
        assertEquals(1, collaboratorRepo.size());
        assertTrue(collaboratorRepo.findBySystemUser(result.systemUser()).isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void registerCompanyCollaborator_whenCompanyNotFound_throwsIllegalArgumentException() {
        AuthzRegistry.authenticationService().authenticate("admin", "Password1");

        controller.registerCompanyCollaborator(
                "user1",
                "Password1",
                "John",
                "Doe",
                "john@email.com",
                new IcaoCode("XXX")
        );
    }

    @Test
    public void registerCompanyCollaborator_whenCompanyNotFound_doesNotSaveCollaborator() {
        AuthzRegistry.authenticationService().authenticate("admin", "Password1");

        final long before = collaboratorRepo.size();

        try {
            controller.registerCompanyCollaborator(
                    "user2",
                    "Password1",
                    "John",
                    "Doe",
                    "john2@email.com",
                    new IcaoCode("XXX")
            );
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
            // expected
        }

        assertEquals(before, collaboratorRepo.size());
    }

    @Test(expected = Exception.class)
    public void registerCompanyCollaborator_whenUnauthenticated_throwsException() {
        controller.registerCompanyCollaborator(
                "user3",
                "Password1",
                "John",
                "Doe",
                "john3@email.com",
                TAP_ICAO
        );
    }

    @Test
    public void repositories_areWiredCorrectly() {
        assertTrue(companyRepo.findByIcaoCode(TAP_ICAO).isPresent());
        assertEquals(0, collaboratorRepo.size());
    }
}