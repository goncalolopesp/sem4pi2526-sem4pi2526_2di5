package pt.isep.sem4pi.infrastructure.persistence;

import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;

/**
 * Minimal repository factory for SEM4PI — provides only user repositories for authz.
 */
public interface RepositoryFactory {

    TransactionalContext newTransactionalContext();

    UserRepository users(TransactionalContext tx);

    UserRepository users();
}

