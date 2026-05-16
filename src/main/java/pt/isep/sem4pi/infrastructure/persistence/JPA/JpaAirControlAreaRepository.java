package pt.isep.sem4pi.infrastructure.persistence.JPA;

import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;
import pt.isep.sem4pi.domain.aircontrolarea.AirControlArea;
import pt.isep.sem4pi.domain.aircontrolarea.AirControlAreaID;
import pt.isep.sem4pi.domain.repositories.AirControlAreaRepository;

public class JpaAirControlAreaRepository
        extends JpaAutoTxRepository<AirControlArea, AirControlAreaID, AirControlAreaID>
        implements AirControlAreaRepository {

    public JpaAirControlAreaRepository(final String persistenceUnitName) {
        super(persistenceUnitName, "acID");
    }

    public JpaAirControlAreaRepository(final TransactionalContext autoTx) {
        super(autoTx, "acID");
    }
}