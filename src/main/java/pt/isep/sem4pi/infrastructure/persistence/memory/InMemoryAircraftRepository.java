package pt.isep.sem4pi.infrastructure.persistence.memory;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.aircraft.Aircraft;
import pt.isep.sem4pi.domain.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.repositories.AircraftRepository;

public class InMemoryAircraftRepository extends InMemoryDomainRepository<Aircraft, AircraftRegistration>
        implements AircraftRepository {
}

