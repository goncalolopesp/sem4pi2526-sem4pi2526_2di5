package pt.isep.sem4pi.infrastructure.persistence;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import pt.isep.sem4pi.domain.model.Manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.Manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;

public class InMemoryManufacturerRepository extends InMemoryDomainRepository<Manufacturer, ManufacturerId> implements ManufacturerRepository {
}
