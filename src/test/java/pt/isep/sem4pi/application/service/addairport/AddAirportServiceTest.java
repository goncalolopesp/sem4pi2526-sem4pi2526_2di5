package pt.isep.sem4pi.application.service.addairport;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.airport.Airport;
import pt.isep.sem4pi.domain.model.airport.Coordinates;
import pt.isep.sem4pi.domain.model.airport.Elevation;
import pt.isep.sem4pi.domain.model.airport.IataCodeTest;
import pt.isep.sem4pi.domain.model.airport.IcaoCode;
import pt.isep.sem4pi.domain.repositories.AirportRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class AddAirportServiceTest {

    private static class FakeAirportRepository implements AirportRepository {
        public final List<Airport> fakeDatabase = new ArrayList<>();

        @Override
        public Airport save(Airport airport) {
            fakeDatabase.add(airport);
            return airport;
        }

        @Override
        public Optional<Airport> findByIataCode(IataCodeTest iataCode) {
            return fakeDatabase.stream().filter(a -> a.identityIata().equals(iataCode)).findFirst();
        }

        @Override
        public Optional<Airport> findByIcaoCode(IcaoCode icaoCode) {
            return fakeDatabase.stream().filter(a -> a.identityIcao().equals(icaoCode)).findFirst();
        }
    }

    private FakeAirportRepository fakeRepo;
    private AddAirportService service;

    @Before
    public void setUp() {
        fakeRepo = new FakeAirportRepository();
        service = new AddAirportService(fakeRepo);
    }

    @Test
    public void ensureSuccessfulRegistrationWhenAirportIsUnique() {
        Airport result = service.addAirport("Francisco Sa Carneiro", "Porto", "Portugal",
                "OPO", "LPPR", 41.24, -8.67, 69.0, "AREA1");

        assertNotNull(result);
        assertEquals("OPO", result.identityIata().code());
        assertEquals(1, fakeRepo.fakeDatabase.size());
    }

    @Test
    public void ensureRegistrationFailsIfIataCodeAlreadyExists() {
        Airport existing = new Airport("Existing", "Town", "Country", new IataCodeTest("OPO"),
                new IcaoCode("ZZZZ"), new Coordinates(0,0), new Elevation(0), "AREA1");
        fakeRepo.save(existing);

        try {
            service.addAirport("Fake Airport", "Fake", "Fake", "OPO", "AAAA", 0, 0, 0, "AREA1");
            fail("Expected an IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("An airport with this IATA code already exists.", e.getMessage());
        }

        assertEquals(1, fakeRepo.fakeDatabase.size());
    }
}