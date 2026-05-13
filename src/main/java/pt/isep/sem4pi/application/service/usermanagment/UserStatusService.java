package pt.isep.sem4pi.application.service.usermanagment;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.validations.Preconditions;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUserRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para gerir o estado (ativo/inativo) dos utilizadores do backoffice.
 *
 * A fonte de verdade do estado é o SystemUser (framework).
 */
public class UserStatusService {

    public record BackofficeUserStatus(BackofficeUser profile, boolean active) {
        public String username() {
            return profile.username();
        }

        public String email() {
            return profile.email();
        }

        public boolean isActive() {
            return active;
        }
    }

    private final BackofficeUserRepository boRepo;
    private final eapli.framework.infrastructure.authz.domain.repositories.UserRepository userRepo;
    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    public UserStatusService() {
        final var repos = PersistenceContext.repositories();
        Preconditions.nonNull(repos, "Persistence repositories must be available");

        this.boRepo = repos.backofficeUsers();
        this.userRepo = repos.users();

        Preconditions.nonNull(boRepo, "BackofficeUser repository must be available");
        Preconditions.nonNull(userRepo, "User repository must be available");
    }

    private void ensureAdmin() {
        Preconditions.nonNull(authz, "Authorization service must be configured");

        if (authz.session().isEmpty() || !authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.ADMIN)) {
            throw new IllegalStateException("Access denied: ADMIN role required");
        }
    }

    /**
     * Lista todos os backoffice users (perfil) com o estado ativo do SystemUser.
     */
    public List<BackofficeUserStatus> listAllWithStatus() {
        ensureAdmin();

        final List<BackofficeUserStatus> result = new ArrayList<>();
        for (final BackofficeUser u : boRepo.findAll()) {
            final boolean active = userRepo.ofIdentity(Username.valueOf(u.username()))
                    .map(SystemUser::isActive)
                    .orElse(false);
            result.add(new BackofficeUserStatus(u, active));
        }
        return result;
    }

    /**
     * Ativa um utilizador (SystemUser.active=true).
     */
    public void enableUser(final String username) {
        ensureAdmin();
        Preconditions.nonEmpty(username, "username is required");

        final Username uname = Username.valueOf(username.trim());
        final Optional<SystemUser> opt = userRepo.ofIdentity(uname);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("System user not found: " + uname);
        }

        final SystemUser user = opt.get();
        if (!user.isActive()) {
            user.activate();
            userRepo.save(user);
        }
    }

    /**
     * Desativa um utilizador (SystemUser.active=false). Não permite desativar o admin em sessão.
     */
    public void disableUser(final String username) {
        ensureAdmin();
        Preconditions.nonEmpty(username, "username is required");

        final Username uname = Username.valueOf(username.trim());

        final Username current = authz.session()
                .map(s -> Username.valueOf(s.authenticatedUser().identity().toString()))
                .orElse(null);

        if (current != null && current.equals(uname) && authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.ADMIN)) {
            throw new IllegalStateException("Cannot disable the currently logged-in administrator");
        }

        final Optional<SystemUser> opt = userRepo.ofIdentity(uname);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("System user not found: " + uname);
        }

        final SystemUser user = opt.get();
        if (user.isActive()) {
            user.deactivate(Calendar.getInstance());
            userRepo.save(user);
        }
    }
}
