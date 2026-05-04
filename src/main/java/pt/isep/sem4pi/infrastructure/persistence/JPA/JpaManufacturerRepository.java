package pt.isep.sem4pi.infrastructure.persistence.JPA;

import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import pt.isep.sem4pi.domain.model.Manufacturer.Manufacturer;
import pt.isep.sem4pi.domain.model.Manufacturer.ManufacturerId;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;

public class JpaManufacturerRepository extends JpaAutoTxRepository<Manufacturer, ManufacturerId, ManufacturerId> implements ManufacturerRepository {
    public JpaManufacturerRepository(final String persistenceUnitName) {
        super(persistenceUnitName, "id");
    }
}
