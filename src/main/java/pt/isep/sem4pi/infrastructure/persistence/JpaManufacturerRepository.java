package pt.isep.sem4pi.infrastructure.persistence;

import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import pt.isep.sem4pi.domain.aircraftModel.AircraftModel;
import pt.isep.sem4pi.domain.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.aircraftModel.Manufacturer;
import pt.isep.sem4pi.domain.aircraftModel.ManufacturerId;
import pt.isep.sem4pi.domain.repositories.ManufacturerRepository;

public class JpaManufacturerRepository extends JpaAutoTxRepository<Manufacturer, ManufacturerId, ManufacturerId> implements ManufacturerRepository {
    public JpaManufacturerRepository(final String persistenceUnitName) {
        super(persistenceUnitName, "id");
    }
}
