package pt.isep.sem4pi.domain.usermanagement;

import java.util.List;
import java.util.Optional;

public interface BackofficeUserRepository {

    BackofficeUser save(BackofficeUser user);

    Optional<BackofficeUser> ofIdentity(String username);

    Optional<BackofficeUser> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /**
     * Returns all backoffice users.
     */
    List<BackofficeUser> findAll();
}
