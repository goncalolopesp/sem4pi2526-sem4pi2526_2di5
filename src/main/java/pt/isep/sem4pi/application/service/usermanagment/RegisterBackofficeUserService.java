package pt.isep.sem4pi.application.service.usermanagment;

import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.validations.Preconditions;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUserRepository;
import pt.isep.sem4pi.domain.usermanagement.Email;
import pt.isep.sem4pi.domain.usermanagement.PhoneNumber;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.domain.usermanagement.UserBuilderHelper;
import pt.isep.sem4pi.infrastructure.persistence.RepositoryFactory;

import java.time.LocalDate;

/**
 * Serviço aplicacional para o caso de uso "Registar utilizador backoffice".
 * <p>
 * Mantém o controller fino e centraliza a coordenação entre repositórios e
 * framework authz (SystemUser).
 */
public class RegisterBackofficeUserService {

    private final RepositoryFactory repoFactory;

    public RegisterBackofficeUserService(final RepositoryFactory repoFactory) {
        Preconditions.nonNull(repoFactory, "RepositoryFactory is required");
        this.repoFactory = repoFactory;
    }

    /**
     * Regista um novo utilizador backoffice.
     * <p>
     * Regras aplicadas (nível aplicacional):
     * - Unicidade de username e email (SystemUser + BackofficeUser).
     * - Criação do SystemUser (framework) + BackofficeUser (perfil de negócio).
     */
    public BackofficeUser register(final String username, final String password,
                                   final String firstName, final String lastName,
                                   final String email, final String phoneNumber,
                                   final LocalDate securityClearanceExpirationDate,
                                   final LocalDate skillsAssessmentDate) {

        Preconditions.nonEmpty(username, "username is required");
        Preconditions.nonEmpty(password, "password is required");
        Preconditions.nonEmpty(email, "email is required");

        final String uname = username.trim();
        final String emailNorm = email.trim().toLowerCase();

        final Email emailVo = Email.valueOf(emailNorm);
        final PhoneNumber phoneVo = PhoneNumber.valueOf(phoneNumber);

        final TransactionalContext tx = repoFactory.newTransactionalContext();

        try {
            tx.beginTransaction();

            final UserRepository users = repoFactory.users(tx);
            final BackofficeUserRepository backoffice = repoFactory.backofficeUsers(tx);

            if (backoffice.existsByUsername(uname)) {
                throw new IllegalArgumentException("username already exists");
            }

            if (backoffice.existsByEmail(emailVo)) {
                throw new IllegalArgumentException("email already exists");
            }

            if (users.ofIdentity(Username.valueOf(uname)).isPresent()) {
                throw new IllegalArgumentException("username already exists");
            }

            final SystemUserBuilder userBuilder = UserBuilderHelper.builder();
            userBuilder.withUsername(uname)
                    .withPassword(password)
                    .withName(firstName, lastName)
                    .withEmail(emailNorm)
                    .withRoles(SEM4PIRoles.BACKOFFICE_OPERATOR);

            final SystemUser sysUser = userBuilder.build();

            final String fullName = ((firstName == null ? "" : firstName)
                    + " "
                    + (lastName == null ? "" : lastName)).trim();

            final BackofficeUser profile = new BackofficeUser(
                    uname,
                    fullName,
                    emailVo,
                    phoneVo,
                    securityClearanceExpirationDate,
                    skillsAssessmentDate
            );

            users.save(sysUser);
            backoffice.save(profile);

            tx.commit();
            return profile;

        } catch (final RuntimeException | Error e) {
            try {
                tx.rollback();
            } catch (final Exception ignored) {
                // rollback best effort
            }
            throw e;
        }
    }
}