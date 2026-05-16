package pt.isep.sem4pi.infrastructure.persistence.JPA;

import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import jakarta.persistence.TypedQuery;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.model.manufacture.ManufacturerId;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;

public class JpaEngineModelRepository extends JpaAutoTxRepository<EngineModel, EngineModelId, EngineModelId> implements EngineModelRepository {

    @Override
    public boolean existsWithManufacturerAndName(ManufacturerId manufacturerId, String name) {
        final TypedQuery<Long> query = entityManager().createQuery(
                "SELECT COUNT(e) FROM EngineModel e WHERE e.manufacturer.id = :manId AND LOWER(e.name) = LOWER(:name)", Long.class);
        query.setParameter("manId", manufacturerId);
        query.setParameter("name", name);
        return query.getSingleResult() > 0;
    }

    public JpaEngineModelRepository(final String persistenceUnitName) {
        super(persistenceUnitName, "id");
    }

}