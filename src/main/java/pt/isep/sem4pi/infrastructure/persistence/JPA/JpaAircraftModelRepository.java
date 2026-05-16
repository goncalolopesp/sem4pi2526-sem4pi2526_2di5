package pt.isep.sem4pi.infrastructure.persistence.JPA;

import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import jakarta.persistence.TypedQuery;
import pt.isep.sem4pi.domain.model.manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.repositories.AircraftModelRepository;

public class JpaAircraftModelRepository extends JpaAutoTxRepository<AircraftModel, AircraftModelId, AircraftModelId> implements AircraftModelRepository {

    public JpaAircraftModelRepository(final String persistenceUnitName) {
        super(persistenceUnitName, "id");
    }
}
