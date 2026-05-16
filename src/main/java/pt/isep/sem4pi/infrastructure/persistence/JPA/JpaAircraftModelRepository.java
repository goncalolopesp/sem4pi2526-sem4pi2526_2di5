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

    @Override
    public boolean containsOfManufacturerAndName(ManufacturerId manufacturerId, String modelName) {
        final TypedQuery<Long> query = entityManager().createQuery(
                "SELECT COUNT(a) FROM AircraftModel a WHERE a.manufacturerId = :manId AND a.modelName = :name",
                Long.class);
        query.setParameter("manId", manufacturerId);
        query.setParameter("name", modelName);

        return query.getSingleResult() > 0;
    }
}
