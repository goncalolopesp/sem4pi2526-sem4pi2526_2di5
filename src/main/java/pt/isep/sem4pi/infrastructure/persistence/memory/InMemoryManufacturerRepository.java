package pt.isep.sem4pi.infrastructure.persistence.memory;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.model.Manufacture.Manufacturer;
import pt.isep.sem4pi.domain.model.Manufacture.ManufacturerId;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;

public class InMemoryManufacturerRepository extends InMemoryDomainRepository<Manufacturer, ManufacturerId>
        implements ManufacturerRepository {
}
