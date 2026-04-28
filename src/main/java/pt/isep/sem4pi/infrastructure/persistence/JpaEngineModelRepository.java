package pt.isep.sem4pi.infrastructure.persistence;

import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import pt.isep.sem4pi.domain.aircraftModel.EngineModel;
import pt.isep.sem4pi.domain.aircraftModel.EngineModelId;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;

public class JpaEngineModelRepository extends JpaAutoTxRepository<EngineModel, EngineModelId, EngineModelId> implements EngineModelRepository {

    public JpaEngineModelRepository(final String persistenceUnitName) {
        super(persistenceUnitName, "id");
    }

}
