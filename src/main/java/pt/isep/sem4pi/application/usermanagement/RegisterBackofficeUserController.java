package pt.isep.sem4pi.application.usermanagement;

import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import eapli.framework.general.domain.model.EmailAddress;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUserRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.domain.usermanagement.UserBuilderHelper;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIPasswordPolicy;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Controlador para registo de utilizadores backoffice.
 *
 * Serve para validar dados e criar tanto o SystemUser (framework) como o BackofficeUser
 * na persistencia. Usado no contexto de gestao de utilizadores (registo).
 */
public class RegisterBackofficeUserController {

    public RegisterBackofficeUserController() {
    }

    /**
     * Garante que a sessao atual pertence a um administrador.
     * Lanca IllegalStateException se nao existir servico de autorizacao ou sessao/role.
     */
    public void ensureAdmin() {
        final AuthorizationService authz = AuthzRegistry.authorizationService();
        if (authz == null) {
            throw new IllegalStateException("Authorization service not configured");
        }
        // check active session
        if (!authz.session().isPresent()) {
            throw new IllegalStateException("Access denied: you must be logged in as ADMIN");
        }
        // check ADMIN role
        if (!authz.isAuthenticatedUserAuthorizedTo(SEM4PIRoles.ADMIN)) {
            throw new IllegalStateException("Access denied: user does not have ADMIN role");
        }
    }

    /**
     * Verifica disponibilidade de username.
     * @param username nome de utilizador a validar
     * @return true se o username for permitido e nao existir na repo
     */
    public boolean isUsernameAvailable(final String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        final var repos = PersistenceContext.repositories();
        if (repos == null) {
            return false;
        }
        final BackofficeUserRepository boRepo = repos.backofficeUsers();
        if (boRepo == null) {
            return false;
        }
        return !boRepo.existsByUsername(username.trim());
    }

    /**
     * Verifica disponibilidade de e-mail (forma e existencia).
     * @param email e-mail a verificar
     * @return true se o e-mail tiver formato valido e nao existir
     */
    public boolean isEmailAvailable(final String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        if (!isEmailFormatValid(email)) {
            return false;
        }
        final var repos = PersistenceContext.repositories();
        if (repos == null) {
            return false;
        }
        final BackofficeUserRepository boRepo = repos.backofficeUsers();
        if (boRepo == null) {
            return false;
        }
        return !boRepo.existsByEmail(email.trim().toLowerCase());
    }

    /**
     * Valida o formato do e-mail.
     * @param email e-mail a validar
     * @return true se o formato e valido
     */
    public boolean isEmailFormatValid(final String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        try {
            EmailAddress.valueOf(email.trim());
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Verifica se uma password satisfaz a politica local.
     * @param password a password a validar
     * @return true se a password cumprir a politica
     */
    public boolean isPasswordValid(final String password) {
        if (password == null) {
            return false;
        }
        final SEM4PIPasswordPolicy policy = new SEM4PIPasswordPolicy();
        return policy.isSatisfiedBy(password);
    }

    /**
     * Procura um BackofficeUser por username.
     * @param username nome de utilizador
     * @return Optional com o BackofficeUser se existir
     */
    public Optional<BackofficeUser> findBackofficeUserByUsername(final String username) {
        if (username == null) {
            return Optional.empty();
        }
        final var repos = PersistenceContext.repositories();
        if (repos == null) {
            return Optional.empty();
        }
        final BackofficeUserRepository boRepo = repos.backofficeUsers();
        if (boRepo == null) {
            return Optional.empty();
        }
        return boRepo.ofIdentity(username.trim());
    }

    /**
     * Regista um novo utilizador backoffice.
     * Regras aplicadas: admin ativo, dados obrigatorios, password valida, unicidade de username/email.
     * @param username username do novo utilizador
     * @param password password em claro
     * @param firstName nome proprio
     * @param lastName apelido
     * @param email e-mail
     * @param phoneNumber contacto
     * @param securityClearanceExpirationDate validade da habilitacao de seguranca
     * @param skillsAssessmentDate data da avaliacao de competencias
     * @return o BackofficeUser criado e persistido
     */
    public BackofficeUser register(final String username, final String password,
                                   final String firstName, final String lastName,
                                   final String email, final String phoneNumber,
                                   final LocalDate securityClearanceExpirationDate,
                                   final LocalDate skillsAssessmentDate) {

        ensureAdmin();

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("username is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("password is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("email is required");
        }

        final SEM4PIPasswordPolicy policy = new SEM4PIPasswordPolicy();
        if (!policy.isSatisfiedBy(password)) {
            throw new IllegalArgumentException("Invalid password: must be at least 6 characters, contain at least one digit and one uppercase letter");
        }

        final var repos = PersistenceContext.repositories();
        if (repos == null) {
            throw new IllegalStateException("PersistenceContext repositories not available");
        }

        final var usersRepo = repos.users();
        if (usersRepo == null) {
            throw new IllegalStateException("User repository not available");
        }

        final BackofficeUserRepository boRepo = repos.backofficeUsers();
        if (boRepo == null) {
            throw new IllegalStateException("BackofficeUser repository not available");
        }

        if (boRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("username already exists");
        }
        if (boRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("email already exists");
        }

        try {
            final SystemUserBuilder userBuilder = UserBuilderHelper.builder();
            userBuilder.withUsername(username)
                    .withPassword(password)
                    .withName(firstName, lastName)
                    .withEmail(email)
                    .withRoles(SEM4PIRoles.BACKOFFICE_OPERATOR);

            final SystemUser sysUser = userBuilder.build();
            usersRepo.save(sysUser);

            final String fullName = (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
            final BackofficeUser bo = new BackofficeUser(
                    username,
                    fullName.trim(),
                    email,
                    phoneNumber,
                    securityClearanceExpirationDate,
                    skillsAssessmentDate
            );
            boRepo.save(bo);

            return bo;
        } catch (final Exception e) {
            throw new IllegalStateException("Error creating backoffice user: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
        }
    }
}
