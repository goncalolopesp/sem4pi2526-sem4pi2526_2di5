package pt.isep.sem4pi.infrastructure.persistence;

import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;

public class JpaEngineModelRepository extends JpaAutoTxRepository<EngineModel, EngineModelId, EngineModelId> implements EngineModelRepository {

    public JpaEngineModelRepository(final String persistenceUnitName) {
        super(persistenceUnitName, "id");
    }

}
