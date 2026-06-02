package pt.isep.sem4pi.application.controller.usermanagement;

import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for listing users in the system. Provides methods to list all users and to list only backoffice users.
 * Access to these methods is restricted to users with the ADMIN role.
 */

public class ListUsersController {

    private final AuthorizationService authorizationService;
    private final UserRepository userRepository;

    /// Constructor for the app
    public ListUsersController() {
        this.authorizationService = AuthzRegistry.authorizationService();
        this.userRepository = PersistenceContext.repositories().users();
    }

    /// Constructor for testing with dependency injection
    public ListUsersController(AuthorizationService authz, UserRepository userRepo) {
        this.authorizationService = authz;
        this.userRepository = userRepo;
    }

    /// List all Users in the system
    public Iterable<SystemUser> allUsers() {
        authorizationService.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.ADMIN);
        return userRepository.findAll();
    }

    /// List the Users that have at least on Backoffice role (ADMIN or BACKOFFICE_OPERATOR)
    public Iterable<SystemUser> allBackofficeUsers() {
        authorizationService.ensureAuthenticatedUserHasAnyOf(SEM4PIRoles.ADMIN);

        Iterable<SystemUser> allUsers = userRepository.findAll();
        List<SystemUser> backofficeUsers = new ArrayList<>();

        for (SystemUser user : allUsers) {
            if (user.hasAny(SEM4PIRoles.ADMIN, SEM4PIRoles.BACKOFFICE_OPERATOR)) {
                backofficeUsers.add(user);
            }
        }

        return backofficeUsers;
    }
}
