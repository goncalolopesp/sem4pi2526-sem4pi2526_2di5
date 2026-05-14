package pt.isep.sem4pi.infrastructure.persistence;

import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUserRepository;

/**
 * Fábrica mínima de repositórios para o projeto SEM4PI.
 *
 * Expõe repositórios necessários para autenticação e gestão de BackofficeUser.
 */
public interface RepositoryFactory {

    TransactionalContext newTransactionalContext();

    UserRepository users(TransactionalContext tx);

    UserRepository users();

    BackofficeUserRepository backofficeUsers(TransactionalContext tx);

    BackofficeUserRepository backofficeUsers();
}
