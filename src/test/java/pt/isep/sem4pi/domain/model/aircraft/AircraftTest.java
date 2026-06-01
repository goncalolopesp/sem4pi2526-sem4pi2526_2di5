package pt.isep.sem4pi.domain.model.aircraft;

import org.junit.Before;
import org.junit.Test;
import pt.isep.sem4pi.domain.aircraft.Aircraft;
import pt.isep.sem4pi.domain.aircraft.AircraftRegistration;
import pt.isep.sem4pi.domain.aircraft.AircraftStatus;
import pt.isep.sem4pi.domain.aircraft.CabinConfiguration;
import pt.isep.sem4pi.domain.model.aircraftModel.AircraftModelId;
import pt.isep.sem4pi.domain.manufacturer.ManufacturerId;

import java.lang.reflect.Constructor;
import java.time.LocalDate;

import static org.junit.Assert.*;

public class AircraftTest {

    private AircraftRegistration registration;
    private AircraftModelId modelId;
    private CabinConfiguration seats;
    private String country;
    private LocalDate validDate;

    @Before
    public void setUp() {
        registration = new AircraftRegistration("CS-TST");
        modelId = new AircraftModelId(new ManufacturerId("AIRBUS"), "A320");
        seats = new CabinConfiguration(150, 20, 0);
        country = "Portugal";
        validDate = LocalDate.now().minusYears(5);
    }

    private Aircraft buildValidAircraft() {
        return new Aircraft(registration, modelId, seats, country, validDate);
    }

    /* Testing Constructors and Validations */

    @Test
    public void testProtectedJPAConstructor() throws Exception {
        Constructor<Aircraft> constructor = Aircraft.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Aircraft aircraft = constructor.newInstance();
        assertNotNull(aircraft);
    }

    @Test
    public void testValidAircraftCreation() {
        Aircraft aircraft = buildValidAircraft();
        assertNotNull(aircraft);
        assertEquals(AircraftStatus.ACTIVE, aircraft.status());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullRegistration() {
        new Aircraft(null, modelId, seats, country, validDate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullModelId() {
        new Aircraft(registration, null, seats, country, validDate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullSeats() {
        new Aircraft(registration, modelId, null, country, validDate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullCountry() {
        new Aircraft(registration, modelId, seats, null, validDate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyCountry() {
        new Aircraft(registration, modelId, seats, "   ", validDate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullDate() {
        new Aircraft(registration, modelId, seats, country, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFutureDate() {
        new Aircraft(registration, modelId, seats, country, LocalDate.now().plusDays(1));
    }

    /* Testing Business Logic and State Changes */

    @Test
    public void testAgeCalculation() {
        Aircraft aircraft = buildValidAircraft();
        assertEquals(5, aircraft.age());
    }

    @Test
    public void testRetireAircraft() {
        Aircraft aircraft = buildValidAircraft();
        assertFalse(aircraft.isRetired());

        aircraft.retire();

        assertTrue(aircraft.isRetired());
        assertEquals(AircraftStatus.RETIRED, aircraft.status());
    }

    @Test(expected = IllegalStateException.class)
    public void testRetireAlreadyRetiredAircraftThrowsException() {
        Aircraft aircraft = buildValidAircraft();
        aircraft.retire();
        aircraft.retire();
    }

    /* Testing Getters and Identity */

    @Test
    public void testGetters() {
        Aircraft aircraft = buildValidAircraft();

        assertEquals(registration, aircraft.registration());
        assertEquals(registration, aircraft.identity());
        assertEquals(modelId, aircraft.modelId());
        assertEquals(seats, aircraft.seats());
    }

    @Test
    public void testSameAs() {
        Aircraft aircraft1 = buildValidAircraft();
        Aircraft aircraft2 = new Aircraft(registration, modelId, seats, country, validDate);
        Aircraft aircraft3 = new Aircraft(new AircraftRegistration("CS-DIF"), modelId, seats, country, validDate);

        assertTrue(aircraft1.sameAs(aircraft2));
        assertTrue(aircraft1.sameAs(aircraft1));
        assertFalse(aircraft1.sameAs(aircraft3));
        assertFalse(aircraft1.sameAs(null));
        assertFalse(aircraft1.sameAs(new Object()));
    }
}