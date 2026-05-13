package pt.isep.sem4pi.application.service.usermanagment;

import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.validations.Preconditions;
import pt.isep.sem4pi.infrastructure.persistence.PersistenceContext;

/**
 * Serviço aplicacional para validar se um utilizador pode aceder ao sistema.
 *
 * Regra de negócio: utilizador inativo (SystemUser.active=false) não deve conseguir autenticar.
 */
public class UserAccessService {

    private final eapli.framework.infrastructure.authz.domain.repositories.UserRepository userRepository;

    public UserAccessService() {
        this.userRepository = PersistenceContext.repositories().users();
    }

    public boolean canUserAccess(final String username) {
        Preconditions.nonEmpty(username, "Username must not be empty");
        Preconditions.nonNull(userRepository, "User repository must be available");

        final var u = userRepository.ofIdentity(Username.valueOf(username.trim()));
        return u.isPresent() && u.get().isActive();
    }
}