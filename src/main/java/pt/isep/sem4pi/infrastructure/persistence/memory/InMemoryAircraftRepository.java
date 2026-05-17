package pt.isep.sem4pi.infrastructure.persistence.memory;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.model.aircraft.Aircraft;
import pt.isep.sem4pi.domain.model.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.repositories.AircraftRepository;

public class InMemoryAircraftRepository extends InMemoryDomainRepository<Aircraft, AircraftRegistration>
        implements AircraftRepository {
}

