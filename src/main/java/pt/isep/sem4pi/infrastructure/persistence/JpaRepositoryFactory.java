package pt.isep.sem4pi.infrastructure.persistence;

import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.authz.repositories.impl.jpa.JpaAutoTxUserRepository;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import pt.isep.sem4pi.domain.aircontrolarea.AirControlArea;
import pt.isep.sem4pi.domain.repositories.AirControlAreaRepository;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;
import pt.isep.sem4pi.domain.usermanagement.BackofficeUserRepository;
import pt.isep.sem4pi.infrastructure.persistence.JPA.*;

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

    @Override
    public AircraftModelRepository aircraftModels() {
        return new JpaAircraftModelRepository(PERSISTENCE_UNIT_NAME);
    }

    @Override
    public EngineModelRepository engineModels() {
        return new JpaEngineModelRepository(PERSISTENCE_UNIT_NAME);
    }

    @Override
    public ManufacturerRepository manufacturers() {
        return new JpaManufacturerRepository(PERSISTENCE_UNIT_NAME);
    }

    @Override
    public AirControlAreaRepository airControlAreas() {
        return new JpaAirControlAreaRepository(PERSISTENCE_UNIT_NAME);
    }

    @Override
    public AirControlAreaRepository airControlAreas(final TransactionalContext tx) {
        return new JpaAirControlAreaRepository(PERSISTENCE_UNIT_NAME);
    }
}