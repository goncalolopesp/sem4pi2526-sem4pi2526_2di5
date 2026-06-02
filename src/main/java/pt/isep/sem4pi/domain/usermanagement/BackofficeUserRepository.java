package pt.isep.sem4pi.domain.usermanagement;

import java.util.List;
import java.util.Optional;

public interface BackofficeUserRepository {

    BackofficeUser save(BackofficeUser user);
    
    /**
     * Remove a BackofficeUser from the repository. Needed to support compensating
     * deletes in non-transactional environments.
     */
    void delete(BackofficeUser user);

    Optional<BackofficeUser> ofIdentity(String username);

    Optional<BackofficeUser> findByEmail(Email email);

    boolean existsByUsername(String username);

    boolean existsByEmail(Email email);

    /**
     * Returns all backoffice users.
     */
    List<BackofficeUser> findAll();
}
