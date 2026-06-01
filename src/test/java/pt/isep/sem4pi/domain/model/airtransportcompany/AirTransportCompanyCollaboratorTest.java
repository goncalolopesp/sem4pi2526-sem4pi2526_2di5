package pt.isep.sem4pi.domain.model.airtransportcompany;

import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.airtransportcompany.*;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.domain.usermanagement.UserBuilderHelper;

import static org.junit.Assert.*;

public class AirTransportCompanyCollaboratorTest {

    private SystemUser testUser;
    private AirTransportCompany testCompany;

    @Before
    public void setUp() {
        testCompany = new AirTransportCompany(
                new CompanyName("TAP Portugal"),
                new IataCodeCompany("TP"),
                new IcaoCodeCompany("TAP")
        );
        testUser = UserBuilderHelper.builder()
                .withUsername("testuser")
                .withPassword("Password1")
                .withName("Test", "User")
                .withEmail("test@domain.com")
                .withRoles(SEM4PIRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)
                .build();
    }

    @Test
    public void constructor_withValidArguments_createsCollaborator() {
        if (testUser == null) return;

        AirTransportCompanyCollaborator collaborator = new AirTransportCompanyCollaborator(testUser, testCompany);

        assertNotNull(collaborator);
        assertEquals(testUser, collaborator.systemUser());
        assertEquals(testCompany, collaborator.company());
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_withNullSystemUser_throwsIllegalArgumentException() {
        new AirTransportCompanyCollaborator(null, testCompany);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_withNullCompany_throwsIllegalArgumentException() {
        new AirTransportCompanyCollaborator(testUser, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_withBothNull_throwsIllegalArgumentException() {
        new AirTransportCompanyCollaborator(null, null);
    }

    @Test
    public void systemUser_returnsAssociatedSystemUser() {
        if (testUser == null) return;

        AirTransportCompanyCollaborator collaborator = new AirTransportCompanyCollaborator(testUser, testCompany);
        assertEquals(testUser, collaborator.systemUser());
    }

    @Test
    public void company_returnsAssociatedCompany() {
        if (testUser == null) return;

        AirTransportCompanyCollaborator collaborator = new AirTransportCompanyCollaborator(testUser, testCompany);
        assertEquals(testCompany, collaborator.company());
    }

    @Test
    public void identity_ofNewCollaborator_isNull() {
        if (testUser == null) return;

        AirTransportCompanyCollaborator collaborator = new AirTransportCompanyCollaborator(testUser, testCompany);

        assertNull(collaborator.identity());
    }

    @Test
    public void sameAs_twoReferencesToSameObject_returnsTrue() {
        if (testUser == null) return;

        AirTransportCompanyCollaborator collaborator = new AirTransportCompanyCollaborator(testUser, testCompany);
        assertTrue(collaborator.sameAs(collaborator));
    }
}