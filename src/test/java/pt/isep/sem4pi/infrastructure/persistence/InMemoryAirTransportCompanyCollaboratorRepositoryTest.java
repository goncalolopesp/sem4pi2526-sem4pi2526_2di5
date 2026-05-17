package pt.isep.sem4pi.infrastructure.persistence;

import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompanyCollaborator;
import pt.isep.sem4pi.domain.model.airtransportcompany.CompanyName;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCode;
import pt.isep.sem4pi.domain.model.airtransportcompany.IcaoCode;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.domain.usermanagement.UserBuilderHelper;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAirTransportCompanyCollaboratorRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class InMemoryAirTransportCompanyCollaboratorRepositoryTest {

    private InMemoryAirTransportCompanyCollaboratorRepository repository;

    private SystemUser user1;
    private SystemUser user2;
    private AirTransportCompany company1;
    private AirTransportCompany company2;
    private AirTransportCompanyCollaborator collaborator1;
    private AirTransportCompanyCollaborator collaborator2;
    private AirTransportCompanyCollaborator collaborator3;

    @Before
    public void setUp() {
        repository = new InMemoryAirTransportCompanyCollaboratorRepository();

        final List<AirTransportCompanyCollaborator> toDelete = new ArrayList<>();
        repository.findAll().forEach(toDelete::add);
        for (AirTransportCompanyCollaborator c : toDelete) {
            repository.delete(c);
        }

        company1 = new AirTransportCompany(
                new CompanyName("TAP Portugal"),
                new IataCode("TP"),
                new IcaoCode("TAP")
        );
        company2 = new AirTransportCompany(
                new CompanyName("Lufthansa"),
                new IataCode("LH"),
                new IcaoCode("DLH")
        );

        user1 = UserBuilderHelper.builder()
                .withUsername("user1")
                .withPassword("Password1")
                .withName("John", "Doe")
                .withEmail("john@email.com")
                .withRoles(SEM4PIRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)
                .build();

        user2 = UserBuilderHelper.builder()
                .withUsername("user2")
                .withPassword("Password1")
                .withName("Jane", "Smith")
                .withEmail("jane@email.com")
                .withRoles(SEM4PIRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)
                .build();

        collaborator1 = new AirTransportCompanyCollaborator(user1, company1);
        collaborator2 = new AirTransportCompanyCollaborator(user2, company1);
        collaborator3 = new AirTransportCompanyCollaborator(user2, company2);
    }

    @Test
    public void findBySystemUser_whenUserExists_returnsCollaborator() {

        repository.save(collaborator1);
        Optional<AirTransportCompanyCollaborator> result = repository.findBySystemUser(user1);

        assertTrue(result.isPresent());
        assertEquals(user1, result.get().systemUser());
    }

    @Test
    public void findBySystemUser_whenUserDoesNotExist_returnsEmpty() {

        repository.save(collaborator1);
        Optional<AirTransportCompanyCollaborator> result = repository.findBySystemUser(user2);

        assertFalse(result.isPresent());
    }

    @Test
    public void findBySystemUser_onEmptyRepository_returnsEmpty() {

        Optional<AirTransportCompanyCollaborator> result = repository.findBySystemUser(user1);
        assertFalse(result.isPresent());
    }

    @Test
    public void findByCompany_whenCompanyHasCollaborators_returnsAll() {

        repository.save(collaborator1);
        repository.save(collaborator2);
        repository.save(collaborator3);

        Iterable<AirTransportCompanyCollaborator> result = repository.findByCompany(company1);

        long count = 0;
        for (AirTransportCompanyCollaborator c : result) {
            assertEquals(company1, c.company());
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void findByCompany_whenCompanyHasNoCollaborators_returnsEmpty() {

        repository.save(collaborator1);

        Iterable<AirTransportCompanyCollaborator> result = repository.findByCompany(company2);
        assertFalse(result.iterator().hasNext());
    }

    @Test
    public void findByCompany_onEmptyRepository_returnsEmpty() {

        Iterable<AirTransportCompanyCollaborator> result = repository.findByCompany(company1);
        assertFalse(result.iterator().hasNext());
    }

    @Test
    public void findByCompany_returnsOnlyCollaboratorsForGivenCompany() {
        repository.save(collaborator1);
        repository.save(collaborator3);

        long count = 0;
        for (AirTransportCompanyCollaborator c : repository.findByCompany(company2)) {
            assertEquals(company2, c.company());
            count++;
        }
        assertEquals(1, count);
    }

    @Test
    public void save_andFindBySystemUser_roundtrip() {

        repository.save(collaborator1);
        Optional<AirTransportCompanyCollaborator> found = repository.findBySystemUser(user1);

        assertTrue(found.isPresent());
        assertSame(collaborator1, found.get());
    }

    @Test
    public void save_multipleCollaborators_allRetrievable() {

        repository.save(collaborator1);
        repository.save(collaborator3);

        assertTrue(repository.findBySystemUser(user1).isPresent());
        assertTrue(repository.findBySystemUser(user2).isPresent());
    }
}