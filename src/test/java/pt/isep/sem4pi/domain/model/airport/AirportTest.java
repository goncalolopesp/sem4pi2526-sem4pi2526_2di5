package pt.isep.sem4pi.domain.model.airport;

import org.junit.Test;

import static org.junit.Assert.*;

public class AirportTest {

    @Test
    public void ensureValidAirportIsCreated() {
        IataCode iata = new IataCode("OPO");
        IcaoCode icao = new IcaoCode("LPPR");
        Coordinates coords = new Coordinates(41.24, -8.67);
        Elevation elev = new Elevation(69.0);

        Airport airport = new Airport("Sa Carneiro", "Porto", "Portugal", iata, icao, coords, elev, "AREA1");

        assertEquals(iata, airport.identityIata());
        assertEquals(icao, airport.identityIcao());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAirportFailsIfNameIsNull() {
        new Airport(null, "Porto", "Portugal", new IataCode("OPO"), new IcaoCode("LPPT"),
                new Coordinates(0,0), new Elevation(0), "AREA1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAirportFailsIfTownIsNull() {
        new Airport("Sa Carneiro", null, "Portugal", new IataCode("OPO"), new IcaoCode("LPPT"),
                new Coordinates(0,0), new Elevation(0), "AREA1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAirportFailsIfAreaIdIsNull() {
        new Airport("Sa Carneiro", "Porto", "Portugal", new IataCode("OPO"), new IcaoCode("LPPT"),
                new Coordinates(0,0), new Elevation(0), null);
    }
}