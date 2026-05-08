package pt.isep.sem4pi.application;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
    * Unit tests for ListUsersController, ensuring it correctly filters users based on roles.
 */
class ListUsersControllerTest {

    private ListUsersController controller;
    private AuthorizationService authzMock;
    private UserRepository userRepoMock;

    @BeforeEach
    void setUp() {
        authzMock = mock(AuthorizationService.class);
        userRepoMock = mock(UserRepository.class);
        controller = new ListUsersController(authzMock, userRepoMock);
    }

    @Test
    void ensureReturnsOnlyBackofficeUsers() {
        /// Create of Users with different roles
        SystemUser adminUser = mock(SystemUser.class);
        when(adminUser.hasAny(SEM4PIRoles.ADMIN, SEM4PIRoles.BACKOFFICE_OPERATOR)).thenReturn(true);

        SystemUser backofficeUser = mock(SystemUser.class);
        when(backofficeUser.hasAny(SEM4PIRoles.ADMIN, SEM4PIRoles.BACKOFFICE_OPERATOR)).thenReturn(true);

        SystemUser clientUser = mock(SystemUser.class);
        when(clientUser.hasAny(SEM4PIRoles.ADMIN, SEM4PIRoles.BACKOFFICE_OPERATOR)).thenReturn(false);

        /// Simulate the BD returning 3 users
        when(userRepoMock.findAll()).thenReturn(Arrays.asList(adminUser, backofficeUser, clientUser));

        /// Execute
        Iterable<SystemUser> result = controller.allBackofficeUsers();

        /// Check if the list returned by the controller contains only the admin and backoffice users
        int count = 0;
        for (SystemUser u : result) {
            count++;
        }

        assertEquals(2, count);
        verify(authzMock, times(1)).ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.ADMIN);
    }
}