package pt.isep.sem4pi.application.usermanagement;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUserRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

public class UserStatusService {

    private final BackofficeUserRepository boRepo;
    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    public UserStatusService() {
        final var repos = PersistenceContext.repositories();
        this.boRepo = repos.backofficeUsers();
    }

    private void ensureAdmin() {
        if (authz == null || authz.session().isEmpty() || !authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.ADMIN)) {
            throw new IllegalStateException("Access denied: ADMIN role required");
        }
    }

    public List<BackofficeUser> listAll() {
        ensureAdmin();
        return boRepo.findAll();
    }

    public void enableUser(final String username) {
        ensureAdmin();
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("username is required");
        }
        final Optional<BackofficeUser> opt = boRepo.ofIdentity(username.trim());
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        final BackofficeUser user = opt.get();
        if (!user.isEnabled()) {
            user.setEnabled(true);
            boRepo.save(user);
        }
    }

    public void disableUser(final String username) {
        ensureAdmin();
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("username is required");
        }
        final Optional<BackofficeUser> opt = boRepo.ofIdentity(username.trim());
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        final BackofficeUser user = opt.get();

        final String current = authz.session().map(s -> s.authenticatedUser().identity().toString()).orElse(null);
        if (current != null && current.equals(user.username()) && authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.ADMIN)) {
            throw new IllegalStateException("Cannot disable the currently logged-in administrator");
        }

        if (user.isEnabled()) {
            user.setEnabled(false);
            boRepo.save(user);
        }
    }
}
