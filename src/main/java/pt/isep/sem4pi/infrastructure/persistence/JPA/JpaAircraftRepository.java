package pt.isep.sem4pi.infrastructure.persistence.JPA;

import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import pt.isep.sem4pi.domain.aircraft.Aircraft;
import pt.isep.sem4pi.domain.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.repositories.AircraftRepository;

public class JpaAircraftRepository extends JpaAutoTxRepository<Aircraft, AircraftRegistration, AircraftRegistration>
        implements AircraftRepository {

    public JpaAircraftRepository(final String persistenceUnitName) {
        super(persistenceUnitName, "id");
    }
}

