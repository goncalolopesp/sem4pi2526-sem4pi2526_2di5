package pt.isep.sem4pi.infrastructure.persistence.memory;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.flight.FlightPlanId;
import pt.isep.sem4pi.domain.repositories.FlightPlanRepository;

public class InMemoryFlightPlanRepository extends InMemoryDomainRepository<FlightPlan, FlightPlanId>
        implements FlightPlanRepository {
}
