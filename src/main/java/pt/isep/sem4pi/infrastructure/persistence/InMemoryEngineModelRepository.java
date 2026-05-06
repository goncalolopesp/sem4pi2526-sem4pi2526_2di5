package pt.isep.sem4pi.infrastructure.persistence;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.model.engineModel.EngineModel;
import pt.isep.sem4pi.domain.model.engineModel.EngineModelId;
import pt.isep.sem4pi.domain.repositories.EngineModelRepository;

public class InMemoryEngineModelRepository extends InMemoryDomainRepository<EngineModel, EngineModelId> implements EngineModelRepository {
}