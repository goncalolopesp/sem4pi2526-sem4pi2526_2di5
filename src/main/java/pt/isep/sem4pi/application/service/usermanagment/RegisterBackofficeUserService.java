package pt.isep.sem4pi.application.service.usermanagment;

import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.SystemUserBuilder;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.validations.Preconditions;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUser;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUserRepository;
import pt.isep.sem4pi.domain.usermanagement.SEM4PIRoles;
import pt.isep.sem4pi.domain.usermanagement.UserBuilderHelper;

import java.time.LocalDate;

/**
 * Serviço aplicacional para o caso de uso "Registar utilizador backoffice".
 *
 * Mantém o controller fino e centraliza a coordenação entre repositórios e
 * framework authz (SystemUser).
 */
public class RegisterBackofficeUserService {

    private final UserRepository usersRepo;
    private final BackofficeUserRepository backofficeRepo;

    public RegisterBackofficeUserService(final UserRepository usersRepo,
                                        final BackofficeUserRepository backofficeRepo) {
        Preconditions.nonNull(usersRepo, "User repository is required");
        Preconditions.nonNull(backofficeRepo, "BackofficeUser repository is required");
        this.usersRepo = usersRepo;
        this.backofficeRepo = backofficeRepo;
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

        // Unicidade: perfil
        if (backofficeRepo.existsByUsername(uname)) {
            throw new IllegalArgumentException("username already exists");
        }
        if (backofficeRepo.existsByEmail(emailNorm)) {
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

        final String fullName = ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
        final BackofficeUser profile = new BackofficeUser(
                uname,
                fullName,
                emailNorm,
                phoneNumber,
                securityClearanceExpirationDate,
                skillsAssessmentDate
        );
        backofficeRepo.save(profile);

        return profile;
    }
}

