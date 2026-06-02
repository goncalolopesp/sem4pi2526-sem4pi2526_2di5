package pt.isep.sem4pi.domain.repositories;

import eapli.framework.domain.repositories.DomainRepository;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.model.route.FlightRouteID;

public interface FlightRouteRepository extends DomainRepository<FlightRouteID, FlightRoute> {
}