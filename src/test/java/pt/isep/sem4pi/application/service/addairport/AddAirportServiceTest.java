package pt.isep.sem4pi.application.service.addairport;

import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;
import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.aircontrolarea.AirControlAreaID;
import pt.isep.sem4pi.domain.model.airport.*;
import pt.isep.sem4pi.domain.repositories.AirportRepository;

import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.Assert.*;

public class AddAirportServiceTest {

    private static class FakeAirportRepository
            extends InMemoryDomainRepository<Airport, IataCodeAirport>
            implements AirportRepository {

        @Override
        public Optional<Airport> findByIcaoCode(IcaoCodeAirport icaoCode) {
            return StreamSupport.stream(findAll().spliterator(), false)
                    .filter(a -> a.identityIcao().equals(icaoCode))
                    .findFirst();
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
    public void ensureSuccessfulRegistrationWhenCodesAreUnique() {
        Airport result = service.addAirport(
                "Francisco Sa Carneiro", "Porto", "Portugal",
                "OPO", "LPPR",
                41.24, -8.67, 69.0,
                "AREA_PT"
        );

        assertNotNull(result);
        assertEquals("OPO", result.identity().code());
        assertEquals("LPPR", result.identityIcao().code());
    }

    @Test
    public void ensureRegistrationFailsIfIataCodeAlreadyExists() {
        // Usamos LISBOA neste teste
        Airport existingAirport = new Airport(
                "Existing Airport", "Lisbon", "Country",
                new IataCodeAirport("LIS"),
                new IcaoCodeAirport("ZZZZ"),
                new Coordinates(0, 0),
                new Elevation(0),
                new AirControlAreaID("AREA_PT")
        );
        fakeRepo.save(existingAirport);

        try {
            service.addAirport(
                    "Fake Airport", "Fake", "Fake",
                    "LIS", "AAAA",
                    0, 0, 0,
                    "AREA_PT"
            );
            fail("Expected an IllegalStateException to be thrown");
        } catch (IllegalStateException e) {
            assertEquals("An airport with this IATA code already exists.", e.getMessage());
        }
    }

    @Test
    public void ensureRegistrationFailsIfIcaoCodeAlreadyExists() {
        Airport existingAirport = new Airport(
                "Existing Airport", "Madrid", "Country",
                new IataCodeAirport("MAD"),
                new IcaoCodeAirport("LEMD"),
                new Coordinates(0, 0),
                new Elevation(0),
                new AirControlAreaID("AREA_PT")
        );
        fakeRepo.save(existingAirport);

        try {
            service.addAirport(
                    "Fake Airport", "Fake", "Fake",
                    "YYY", "LEMD", // ICAO Duplicado
                    0, 0, 0,
                    "AREA_PT"
            );
            fail("Expected an IllegalStateException to be thrown");
        } catch (IllegalStateException e) {
            assertEquals("An airport with this ICAO code already exists.", e.getMessage());
        }
    }
}