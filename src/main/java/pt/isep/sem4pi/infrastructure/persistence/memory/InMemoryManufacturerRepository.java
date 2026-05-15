package pt.isep.sem4pi.infrastructure.persistence.memory;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.model.manufacture.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacture.ManufacturerId;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;

public class InMemoryManufacturerRepository extends InMemoryDomainRepository<Manufacturer, ManufacturerId>
        implements ManufacturerRepository {
}
