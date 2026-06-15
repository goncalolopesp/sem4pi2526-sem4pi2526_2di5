package pt.isep.sem4pi.domain.repositories;

import eapli.framework.domain.repositories.DomainRepository;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.flight.FlightPlanId;

public interface FlightPlanRepository extends DomainRepository<FlightPlanId, FlightPlan> {



}
