package pt.isep.sem4pi.application.service.airtransportcompany;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.CompanyName;
import pt.isep.sem4pi.domain.model.shared.IataCode;
import pt.isep.sem4pi.domain.model.shared.IcaoCode;
import pt.isep.sem4pi.domain.repositories.AirTransportCompanyRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class RegisterAirTransportCompanyServiceTest {

    private static class FakeAirTransportCompanyRepository implements AirTransportCompanyRepository {

        public final List<AirTransportCompany> fakeDatabase = new ArrayList<>();

        @Override
        public AirTransportCompany save(AirTransportCompany company) {
            fakeDatabase.add(company);
            return company;
        }

        @Override
        public Optional<AirTransportCompany> findByIataCode(IataCode iataCode) {
            return fakeDatabase.stream()
                    .filter(c -> c.identityIata().equals(iataCode))
                    .findFirst();
        }

        @Override
        public Optional<AirTransportCompany> findByIcaoCode(IcaoCode icaoCode) {
            return fakeDatabase.stream()
                    .filter(c -> c.identityIcao().equals(icaoCode))
                    .findFirst();
        }

        @Override
        public Iterable<AirTransportCompany> findAll() {
            return fakeDatabase;
        }
    }

    private FakeAirTransportCompanyRepository fakeRepo;
    private RegisterAirTransportCompanyService service;

    @Before
    public void setUp() {
        fakeRepo = new FakeAirTransportCompanyRepository();
        service = new RegisterAirTransportCompanyService(fakeRepo);
    }

    @Test
    public void ensureSuccessfulRegistrationWhenCodesAreUnique() {
        AirTransportCompany result = service.registerCompany("Emirates", "EK", "UAE");

        assertNotNull(result);
        assertEquals("EK", result.identityIata().code());

        assertEquals(1, fakeRepo.fakeDatabase.size());
    }

    @Test
    public void ensureRegistrationFailsIfIataCodeAlreadyExists() {
        AirTransportCompany existingCompany = new AirTransportCompany(
                new CompanyName("Existing Airline"),
                new IataCode("EK"),
                new IcaoCode("AAA")
        );
        fakeRepo.save(existingCompany);

        try {
            service.registerCompany("Emirates", "EK", "UAE");
            fail("Expected an IllegalStateException to be thrown");
        } catch (IllegalStateException e) {
            assertEquals("A company with this IATA code already exists.", e.getMessage());
        }

        assertEquals(1, fakeRepo.fakeDatabase.size());
    }

    @Test
    public void ensureRegistrationFailsIfIcaoCodeAlreadyExists() {
        AirTransportCompany existingCompany = new AirTransportCompany(
                new CompanyName("Existing Airline"),
                new IataCode("ZZ"),
                new IcaoCode("UAE")
        );
        fakeRepo.save(existingCompany);

        try {
            service.registerCompany("Emirates", "EK", "UAE");
            fail("Expected an IllegalStateException to be thrown");
        } catch (IllegalStateException e) {
            assertEquals("A company with this ICAO code already exists.", e.getMessage());
        }

        assertEquals(1, fakeRepo.fakeDatabase.size());
    }
}