package pt.isep.sem4pi.application.service.aircraft;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.model.airtransportcompany.AirTransportCompany;
import pt.isep.sem4pi.domain.model.airtransportcompany.CompanyName;
import pt.isep.sem4pi.domain.model.airtransportcompany.IataCode;
import pt.isep.sem4pi.domain.model.airtransportcompany.IcaoCode;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAirTransportCompanyRepository;
import pt.isep.sem4pi.infrastructure.persistence.memory.InMemoryAircraftModelTempRepository;

import static org.junit.Assert.*;

public class AddAircraftServiceTest {

    private InMemoryAirTransportCompanyRepository companyRepo;
    private InMemoryAircraftModelTempRepository modelRepo;
    private AddAircraftService service;

    @Before
    public void setUp() {
        companyRepo = new InMemoryAirTransportCompanyRepository();
        modelRepo = new InMemoryAircraftModelTempRepository();
        service = new AddAircraftService(companyRepo, modelRepo);

        companyRepo.save(new AirTransportCompany(new CompanyName("TAP"), new IataCode("TP"), new IcaoCode("TAP")));
    }

    @Test
    public void ensureSuccessfulAircraftAddition() {
        service.addAircraftToFleet("TP", "CS-TPA", "A320", "Portugal", 150, 0, 0);

        AirTransportCompany tap = companyRepo.findByIataCode(new IataCode("TP")).get();
        assertEquals(1, tap.getFleet().size());
    }

    @Test
    public void ensureFailsIfSeatsExceedModelCapacity() {
        try {
            service.addAircraftToFleet("TP", "CS-TPB", "A320", "Portugal", 200, 0, 0);
            fail("Expected exception");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("exceed the maximum capacity"));
        }
    }

    @Test
    public void ensureFailsIfRegistrationAlreadyExists() {
        service.addAircraftToFleet("TP", "CS-TPA", "A320", "Portugal", 150, 0, 0);

        try {
            service.addAircraftToFleet("TP", "CS-TPA", "A320", "Portugal", 100, 0, 0);
            fail("Expected exception");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("already exists"));
        }
    }
}