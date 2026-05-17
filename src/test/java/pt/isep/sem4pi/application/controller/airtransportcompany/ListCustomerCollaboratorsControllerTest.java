package pt.isep.sem4pi.application.controller.airtransportcompany;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.application.controller.airtransportcompany.ListCustomerCollaboratorsController.CollaboratorView;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompanyCollaborator;
import pt.isep.sem4pi.domain.model.airtransportcompany.CompanyName;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCode;
import pt.isep.sem4pi.domain.model.airtransportcompany.IcaoCode;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.domain.usermanagement.UserBuilderHelper;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAirTransportCompanyCollaboratorRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAirTransportCompanyRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory;

import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

public class ListCustomerCollaboratorsControllerTest {

    private InMemoryAirTransportCompanyRepository companyRepo;
    private InMemoryAirTransportCompanyCollaboratorRepository collaboratorRepo;
    private ListCustomerCollaboratorsController controller;
    private RegisterCustomerCollaboratorController registerController;

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
        controller = new ListCustomerCollaboratorsController(companyRepo, collaboratorRepo);
        registerController = new RegisterCustomerCollaboratorController(companyRepo, collaboratorRepo);

        companyRepo.save(new AirTransportCompany(
                new CompanyName("TAP Portugal"),
                new IataCode("TP"),
                TAP_ICAO
        ));
    }

    @Test
    public void listCompanyCollaborators_whenAuthenticated_returnsOnlyActiveCollaborators() {
        AuthzRegistry.authenticationService().authenticate("admin", "Password1");

        registerController.registerCompanyCollaborator(
                "active1", "Password1", "John", "Doe", "john@email.com", TAP_ICAO);
        registerController.registerCompanyCollaborator(
                "active2", "Password1", "Jane", "Smith", "jane@email.com", TAP_ICAO);
        registerController.registerCompanyCollaborator(
                "disabled1", "Password1", "Bob", "Inactive", "bob@email.com", TAP_ICAO);

        final var userRepo = PersistenceContext.repositories().users();
        final var disabledUser = userRepo.ofIdentity(Username.valueOf("disabled1")).orElseThrow();
        disabledUser.deactivate(Calendar.getInstance());
        userRepo.save(disabledUser);

        final List<CollaboratorView> result = controller.listCompanyCollaborators(TAP_ICAO);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.username().equals("active1")));
        assertTrue(result.stream().anyMatch(c -> c.username().equals("active2")));
        assertFalse(result.stream().anyMatch(c -> c.username().equals("disabled1")));
    }

    @Test
    public void listCompanyCollaborators_whenNoCollaborators_returnsEmptyList() {
        AuthzRegistry.authenticationService().authenticate("admin", "Password1");

        final List<CollaboratorView> result = controller.listCompanyCollaborators(TAP_ICAO);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void listCompanyCollaborators_whenCompanyNotFound_throwsIllegalArgumentException() {
        AuthzRegistry.authenticationService().authenticate("admin", "Password1");

        controller.listCompanyCollaborators(new IcaoCode("XXX"));
    }

    @Test(expected = Exception.class)
    public void listCompanyCollaborators_whenUnauthenticated_throwsException() {
        controller.listCompanyCollaborators(TAP_ICAO);
    }

    @Test
    public void listCompanyCollaborators_mapsUserDataCorrectly() {
        AuthzRegistry.authenticationService().authenticate("admin", "Password1");

        registerController.registerCompanyCollaborator(
                "collab1", "Password1", "Alice", "Wonder", "alice@email.com", TAP_ICAO);

        final List<CollaboratorView> result = controller.listCompanyCollaborators(TAP_ICAO);

        assertEquals(1, result.size());
        assertEquals("collab1", result.get(0).username());
        assertEquals("Alice", result.get(0).firstName());
        assertEquals("Wonder", result.get(0).lastName());
        assertEquals("alice@email.com", result.get(0).email());
    }
}