package pt.isep.sem4pi.application.controller.usermanagement;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.validations.Preconditions;
import pt.isep.sem4pi.application.service.usermanagment.RegisterBackofficeUserService;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUserRepository;
import pt.isep.sem4pi.domain.usermanagement.Email;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Controlador para registo de utilizadores backoffice.
 *
 * A UI chama este controller. O controller valida precondições (ex: permissões)
 * e delega a coordenação do caso de uso para um serviço aplicacional.
 */
public class RegisterBackofficeUserController {

    public RegisterBackofficeUserController() {
        // empty
    }

    /**
     * Garante que a sessão atual pertence a um administrador.
     */
    public void ensureAdmin() {
        final AuthorizationService authz = AuthzRegistry.authorizationService();
        Preconditions.nonNull(authz, "Authorization service not configured");

        if (authz.session().isEmpty() || !authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.ADMIN)) {
            throw new IllegalStateException("Access denied: ADMIN role required");
        }
    }

    /**
     * Verifica disponibilidade de username (para feedback imediato na UI).
     */
    public boolean isUsernameAvailable(final String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        final var repos = PersistenceContext.repositories();
        final var boRepo = repos.backofficeUsers();

        // Nota: a confirmação final é feita no serviço na operação de registo
        return !boRepo.existsByUsername(username.trim());
    }

    /**
     * Verifica disponibilidade de e-mail (para feedback imediato na UI).
     */
    public boolean isEmailAvailable(final String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        if (!isEmailFormatValid(email)) {
            return false;
        }

        final var repos = PersistenceContext.repositories();
        final BackofficeUserRepository boRepo = repos.backofficeUsers();

        // Nota: a confirmação final é feita no serviço na operação de registo
        return !boRepo.existsByEmail(Email.valueOf(email.trim()));
    }

    public boolean isEmailFormatValid(final String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        try {
            Email.valueOf(email.trim());
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    public boolean isPasswordValid(final String password) {
        if (password == null) {
            return false;
        }
        return new SEM4PIPasswordPolicy().isSatisfiedBy(password);
    }

    public Optional<BackofficeUser> findBackofficeUserByUsername(final String username) {
        if (username == null) {
            return Optional.empty();
        }
        final var repos = PersistenceContext.repositories();
        return repos.backofficeUsers().ofIdentity(username.trim());
    }

    /**
     * Regista um novo utilizador backoffice.
     */
    public BackofficeUser register(final String username, final String password,
                                   final String firstName, final String lastName,
                                   final String email, final String phoneNumber,
                                   final LocalDate securityClearanceExpirationDate,
                                   final LocalDate skillsAssessmentDate) {

        ensureAdmin();

        Preconditions.nonEmpty(username, "username is required");
        Preconditions.nonEmpty(password, "password is required");
        Preconditions.nonEmpty(email, "email is required");

        // Para UX: validação rápida de policy antes de tentar criar o SystemUser
        if (!new SEM4PIPasswordPolicy().isSatisfiedBy(password)) {
            throw new IllegalArgumentException(
                    "Invalid password: must be at least 6 characters, contain at least one digit and one uppercase letter");
        }

        final var repos = PersistenceContext.repositories();
        final var service = new RegisterBackofficeUserService(repos);

        return service.register(username, password, firstName, lastName, email, phoneNumber,
                securityClearanceExpirationDate, skillsAssessmentDate);
    }
}
