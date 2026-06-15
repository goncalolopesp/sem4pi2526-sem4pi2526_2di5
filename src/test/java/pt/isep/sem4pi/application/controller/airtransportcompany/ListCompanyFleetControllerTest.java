package pt.isep.sem4pi.application.controller.airtransportcompany;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.application.controller.airtransportcompany.ListCompanyFleetController.FleetSortOption;
import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.model.aircraft.CabinConfiguration;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompanyCollaborator;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyCollaboratorRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ListCompanyFleetControllerTest {

    private ListCompanyFleetController controller;
    private AirTransportCompanyCollaboratorRepository collabRepoMock;

    @Before
    public void setUp() {
        // 1. Iniciar Base de Dados em Memória para o EAPLI não dar erro de Sessão
        System.setProperty("persistence.repositoryFactory", "pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryRepositoryFactory");
        PersistenceContext.resetForTesting();
        AuthzRegistry.configure(PersistenceContext.repositories().users(), new SEM4PIPasswordPolicy(), new PlainTextEncoder());

        if (AuthzRegistry.authorizationService().hasSession()) {
            AuthzRegistry.authorizationService().clearSession();
        }

        // 2. Criar e autenticar um User Real para contornar a classe 'final' UserSession
        SystemUserBuilder builder = new SystemUserBuilder(new SEM4PIPasswordPolicy(), new PlainTextEncoder());
        SystemUser realUser = builder.withUsername("test_collab")
                .withPassword("Password1")
                .withName("Test", "User")
                .withEmail("test@tap.pt")
                .withRoles(SEM4PIRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)
                .build();
        PersistenceContext.repositories().users().save(realUser);
        AuthzRegistry.authenticationService().authenticate("test_collab", "Password1");

        // 3. Mockar apenas o teu repositório e a frota
        collabRepoMock = mock(AirTransportCompanyCollaboratorRepository.class);
        AirTransportCompanyCollaborator collabMock = mock(AirTransportCompanyCollaborator.class);
        AirTransportCompany companyMock = mock(AirTransportCompany.class);

        when(collabRepoMock.findBySystemUser(any(SystemUser.class))).thenReturn(Optional.of(collabMock));
        when(collabMock.company()).thenReturn(companyMock);

        Aircraft a1 = mockAircraft("CS-ZZZ", "BOEING", "B737", 200, 10);
        Aircraft a2 = mockAircraft("CS-AAA", "AIRBUS", "A320", 150, 5);
        Aircraft a3 = mockAircraft("CS-MMM", "EMBRAER", "E190", 100, 15);

        when(companyMock.getFleet()).thenReturn(Arrays.asList(a1, a2, a3));

        // Injetar o AuthzService real (com a sessão ativa) e o repo falso
        controller = new ListCompanyFleetController(AuthzRegistry.authorizationService(), collabRepoMock);
    }

    private Aircraft mockAircraft(String reg, String maker, String model, int capacity, int age) {
        Aircraft aircraft = mock(Aircraft.class);

        AircraftRegistration registration = mock(AircraftRegistration.class);
        when(registration.toString()).thenReturn(reg);
        when(aircraft.identity()).thenReturn(registration);

        AircraftModelId modelId = mock(AircraftModelId.class);
        ManufacturerId manId = mock(ManufacturerId.class);
        when(manId.toString()).thenReturn(maker);
        when(modelId.manufacturerId()).thenReturn(manId);
        when(modelId.modelName()).thenReturn(model);
        when(aircraft.modelId()).thenReturn(modelId);

        CabinConfiguration cabin = mock(CabinConfiguration.class);
        when(cabin.getTotalCapacity()).thenReturn(capacity);
        when(aircraft.seats()).thenReturn(cabin);

        when(aircraft.age()).thenReturn(age);

        return aircraft;
    }

    @Test
    public void ensureSortByRegistrationWorks() {
        Iterable<Aircraft> result = controller.listMyCompanyFleet(FleetSortOption.REGISTRATION);
        Iterator<Aircraft> iterator = result.iterator();

        assertEquals("CS-AAA", iterator.next().identity().toString());
        assertEquals("CS-MMM", iterator.next().identity().toString());
        assertEquals("CS-ZZZ", iterator.next().identity().toString());
    }

    @Test
    public void ensureSortByMakerWorks() {
        Iterable<Aircraft> result = controller.listMyCompanyFleet(FleetSortOption.MAKER);
        Iterator<Aircraft> iterator = result.iterator();

        assertEquals("AIRBUS", iterator.next().modelId().manufacturerId().toString());
        assertEquals("BOEING", iterator.next().modelId().manufacturerId().toString());
        assertEquals("EMBRAER", iterator.next().modelId().manufacturerId().toString());
    }

    @Test
    public void ensureSortByModelWorks() {
        Iterable<Aircraft> result = controller.listMyCompanyFleet(FleetSortOption.MODEL);
        Iterator<Aircraft> iterator = result.iterator();

        assertEquals("A320", iterator.next().modelId().modelName());
        assertEquals("B737", iterator.next().modelId().modelName());
        assertEquals("E190", iterator.next().modelId().modelName());
    }

    @Test
    public void ensureSortByCapacityWorks() {
        Iterable<Aircraft> result = controller.listMyCompanyFleet(FleetSortOption.CAPACITY);
        Iterator<Aircraft> iterator = result.iterator();

        assertEquals(100, iterator.next().seats().getTotalCapacity());
        assertEquals(150, iterator.next().seats().getTotalCapacity());
        assertEquals(200, iterator.next().seats().getTotalCapacity());
    }

    @Test
    public void ensureSortByAgeWorks() {
        Iterable<Aircraft> result = controller.listMyCompanyFleet(FleetSortOption.AGE);
        Iterator<Aircraft> iterator = result.iterator();

        assertEquals(5, iterator.next().age());
        assertEquals(10, iterator.next().age());
        assertEquals(15, iterator.next().age());
    }

    @Test(expected = IllegalStateException.class)
    public void ensureThrowsExceptionWhenCollaboratorNotFound() {
        when(collabRepoMock.findBySystemUser(any(SystemUser.class))).thenReturn(Optional.empty());
        controller.listMyCompanyFleet(FleetSortOption.AGE);
    }

    @Test
    public void ensureEnumCoverage() {
        FleetSortOption option = FleetSortOption.valueOf("MAKER");
        assertEquals(FleetSortOption.MAKER, option);

        FleetSortOption[] values = FleetSortOption.values();
        assertTrue(values.length > 0);
    }

    @Test
    public void ensureDefaultConstructorWorks() {
        ListCompanyFleetController defaultController = new ListCompanyFleetController();
        assertNotNull(defaultController);
    }
}
