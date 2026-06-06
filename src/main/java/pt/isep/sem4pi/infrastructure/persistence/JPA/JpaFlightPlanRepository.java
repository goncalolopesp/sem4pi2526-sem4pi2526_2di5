package pt.isep.sem4pi.infrastructure.persistence.JPA;

import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import pt.isep.sem4pi.domain.model.flight.FlightPlan;
import pt.isep.sem4pi.domain.model.flight.FlightPlanId;
import pt.isep.sem4pi.domain.repositories.FlightPlanRepository;

public class JpaFlightPlanRepository
        extends JpaAutoTxRepository<FlightPlan, FlightPlanId, FlightPlanId>
        implements FlightPlanRepository {

    public JpaFlightPlanRepository(final String persistenceUnitName) {
        super(persistenceUnitName, "flightPlanId");
    }
}
