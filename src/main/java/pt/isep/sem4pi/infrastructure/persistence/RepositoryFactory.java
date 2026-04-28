package pt.isep.sem4pi.infrastructure.persistence;

import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;

/**
 * Minimal repository factory for SEM4PI — provides only user repositories for authz.
 */
public interface RepositoryFactory {

    TransactionalContext newTransactionalContext();

    UserRepository users(TransactionalContext tx);

    UserRepository users();

    AircraftModelRepository aircraftModels();

    EngineModelRepository engineModels();

    ManufacturerRepository manufacturers();
}

