package pt.isep.sem4pi.infrastructure.persistence.memory;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.repositories.FlightRouteRepository;
import pt.isep.sem4pi.domain.route.FlightRoute;
import pt.isep.sem4pi.domain.route.FlightRouteID;

public class InMemoryFlightRouteRepository extends InMemoryDomainRepository<FlightRoute, FlightRouteID> implements FlightRouteRepository {
}
