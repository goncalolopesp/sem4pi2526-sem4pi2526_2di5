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
 *
 * Mantém o controller fino e centraliza a coordenação entre repositórios e
 * framework authz (SystemUser).
 */
public class RegisterBackofficeUserService {

    private final RepositoryFactory repositoryFactory;

    public RegisterBackofficeUserService(final RepositoryFactory repositoryFactory) {
        Preconditions.nonNull(repositoryFactory, "RepositoryFactory is required");
        this.repositoryFactory = repositoryFactory;
    }

    /**
     * Regista um novo utilizador backoffice.
     *
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

        final TransactionalContext tx = repositoryFactory.newTransactionalContext();
        tx.beginTransaction();
        SystemUser createdSysUser = null;
        UserRepository usersRepo = null;
        try {
            usersRepo = repositoryFactory.users(tx);
            final BackofficeUserRepository backofficeRepo = repositoryFactory.backofficeUsers(tx);

            // Unicidade: perfil
            if (backofficeRepo.existsByUsername(uname)) {
                throw new IllegalArgumentException("username already exists");
            }
            if (backofficeRepo.existsByEmail(emailVo)) {
                throw new IllegalArgumentException("email already exists");
            }

            // Unicidade: SystemUser (framework)
            if (usersRepo.ofIdentity(Username.valueOf(uname)).isPresent()) {
                throw new IllegalArgumentException("username already exists");
            }

            // Criação do SystemUser valida automaticamente password policy via builder helper
            final SystemUserBuilder userBuilder = UserBuilderHelper.builder();
            userBuilder.withUsername(uname)
                    .withPassword(password)
                    .withName(firstName, lastName)
                    .withEmail(emailNorm)
                    .withRoles(SEM4PIRoles.BACKOFFICE_OPERATOR);

            final SystemUser sysUser = userBuilder.build();
            usersRepo.save(sysUser);
            createdSysUser = sysUser;

            final String fullName = ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
            final BackofficeUser profile = new BackofficeUser(
                    uname,
                    fullName,
                    emailVo,
                    phoneVo,
                    securityClearanceExpirationDate,
                    skillsAssessmentDate
            );
            backofficeRepo.save(profile);

            tx.commit();
            return profile;
        } catch (final RuntimeException | Error e) {
            try {
                tx.rollback();
            } catch (final Exception ex) {
                // swallow rollback exceptions but log if required
            }

            if (createdSysUser != null) {
                try {
                    // attempt compensating delete using same repo instance used in the transaction
                    if (usersRepo != null) {
                        usersRepo.delete(createdSysUser);
                    } else {
                        // fallback: use repository factory
                        repositoryFactory.users().delete(createdSysUser);
                    }
                } catch (final Exception ex) {
                    // swallow - best-effort compensating action
                }
            }

            throw e;
        } finally {
            try {
                tx.close();
            } catch (final Exception ex) {
                // swallow close exceptions
            }
        }
    }
}
