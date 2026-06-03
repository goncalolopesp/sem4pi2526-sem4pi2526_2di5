package pt.isep.sem4pi.domain.model.airport;

import org.junit.Test;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlAreaID;

import static org.junit.Assert.*;

public class AirportTest {

    @Test
    public void ensureValidAirportIsCreated() {
        IataCodeAirport iata = new IataCodeAirport("OPO");
        IcaoCodeAirport icao = new IcaoCodeAirport("LPPR");
        Coordinates coords = new Coordinates(41.24, -8.67);
        Elevation elev = new Elevation(69.0);
        AirControlAreaID areaId = new AirControlAreaID("AREA1");

        Airport airport = new Airport("Sa Carneiro", "Porto", "Portugal", iata, icao, coords, elev, areaId);

        assertEquals(iata, airport.identity());
        assertEquals(icao, airport.identityIcao());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAirportFailsIfNameIsNull() {
        new Airport(null, "Porto", "Portugal", new IataCodeAirport("OPO"), new IcaoCodeAirport("LPPT"),
                new Coordinates(0,0), new Elevation(0), new AirControlAreaID("AREA1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAirportFailsIfTownIsNull() {
        new Airport("Sa Carneiro", null, "Portugal", new IataCodeAirport("OPO"), new IcaoCodeAirport("LPPT"),
                new Coordinates(0,0), new Elevation(0), new AirControlAreaID("AREA1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAirportFailsIfAreaIdIsNull() {
        new Airport("Sa Carneiro", "Porto", "Portugal", new IataCodeAirport("OPO"), new IcaoCodeAirport("LPPT"),
                new Coordinates(0,0), new Elevation(0), null);
    }
}