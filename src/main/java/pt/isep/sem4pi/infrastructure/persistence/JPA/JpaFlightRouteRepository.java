package pt.isep.sem4pi.infrastructure.persistence.JPA;

import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import pt.isep.sem4pi.domain.repositories.FlightRouteRepository;
import pt.isep.sem4pi.domain.model.route.FlightRoute;
import pt.isep.sem4pi.domain.model.route.FlightRouteID;

/**
 * JPA repository implementation for Flight Route.
 * By extending JpaAutoTxRepository, the framework automatically handles
 * transactions and provides methods like save(), findAll(), and ofIdentity().
 */
public class JpaFlightRouteRepository
        extends JpaAutoTxRepository<FlightRoute, FlightRouteID, FlightRouteID>
        implements FlightRouteRepository {

    public JpaFlightRouteRepository() {
        // The first parameter is the Persistence Unit name.
        // The second parameter is the exact name of the @EmbeddedId field in the FlightRoute class.
        super("sem4piPU", "routeID");
    }
}