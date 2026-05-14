package pt.isep.sem4pi.infrastructure.persistence;

import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.authz.repositories.impl.jpa.JpaAutoTxUserRepository;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUserRepository;
import pt.isep.sem4pi.infrastructure.persistence.JPA.JpaBackofficeUserRepository;

public class JpaRepositoryFactory implements RepositoryFactory {

    private static final String PERSISTENCE_UNIT_NAME = "sem4piPU";

    @Override
    public TransactionalContext newTransactionalContext() {
        return JpaAutoTxRepository.buildTransactionalContext(PERSISTENCE_UNIT_NAME);
    }

    @Override
    public UserRepository users(final TransactionalContext tx) {
        return new JpaAutoTxUserRepository(tx);
    }

    @Override
    public UserRepository users() {
        return new JpaAutoTxUserRepository(PERSISTENCE_UNIT_NAME, new java.util.HashMap<>());
    }

    @Override
    public BackofficeUserRepository backofficeUsers(final TransactionalContext tx) {
        return new JpaBackofficeUserRepository(PERSISTENCE_UNIT_NAME);
    }

    @Override
    public BackofficeUserRepository backofficeUsers() {
        return new JpaBackofficeUserRepository(PERSISTENCE_UNIT_NAME);
    }
}