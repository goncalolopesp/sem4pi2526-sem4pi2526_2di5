package pt.isep.sem4pi.domain.model.pilot;

import org.junit.Test;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCodeCompany;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PilotTest {

    @Test
    public void ensureOwnershipUsesAssignedUsernameCaseInsensitively() {
        final Pilot pilot = new Pilot(
                new PilotId("PILOT01"), new IataCodeCompany("TP"), "flight.pilot");

        assertTrue(pilot.isOwnedBy("FLIGHT.PILOT"));
        assertFalse(pilot.isOwnedBy("another.pilot"));
    }
}
