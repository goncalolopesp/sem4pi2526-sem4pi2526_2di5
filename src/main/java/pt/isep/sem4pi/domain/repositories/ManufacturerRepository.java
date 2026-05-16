package pt.isep.sem4pi.domain.repositories;

import eapli.framework.domain.repositories.DomainRepository;
import pt.isep.sem4pi.domain.model.manufacture.Manufacturer;
import pt.isep.sem4pi.domain.model.manufacture.ManufacturerId;

public interface ManufacturerRepository extends DomainRepository<ManufacturerId, Manufacturer> {
}
