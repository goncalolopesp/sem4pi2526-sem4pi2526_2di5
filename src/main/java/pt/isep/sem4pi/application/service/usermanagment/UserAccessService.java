package pt.isep.sem4pi.application.service.usermanagment;

import eapli.framework.validations.Preconditions;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUserRepository;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

/**
 * Serviço aplicacional para validar se um utilizador pode aceder ao sistema.
 *
 * A regra de negócio "utilizador desativado não pode autenticar"
 * não pertence à UI.
 */
public class UserAccessService {

    private final BackofficeUserRepository userRepository;

    public UserAccessService() {
        this.userRepository = PersistenceContext.repositories().backofficeUsers();
    }

    public boolean canUserAccess(final String username) {
        Preconditions.nonEmpty(username, "Username must not be empty");
        Preconditions.nonNull(userRepository, "User repository must be available");

        final var user = userRepository.ofIdentity(username.trim());

        return user.isPresent() && user.get().canAuthenticate();
    }
}