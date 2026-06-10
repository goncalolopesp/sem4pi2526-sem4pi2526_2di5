package pt.isep.sem4pi.us081;

import org.junit.Test;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanDTO;
import pt.isep.sem4pi.domain.model.flight.DTO.CreateFlightPlanLegDTO;
import pt.isep.sem4pi.us120.US120FlightPlanValidator;
import pt.isep.sem4pi.us120.US120ValidationResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class US081FlightPlanMapperTest {

    @Test
    public void ensureMapperBuildsDtoFromValidParseTree() throws Exception {
        final int year = LocalDate.now().getYear();
        final Path file = Files.createTempFile("flight-plan", ".dsldig5");
        Files.writeString(file, """
                flight TP100 charter {
                aircraft CS-TST;
                pilot PILOT01;
                route TP100 { (41.0|-8.0) (40.0|-3.0) };
                leg {
                departure { OPO, 15/6/%d, 09:30:15 };
                arrival { MAD, 11:45 };
                segment { (41.0|-8.0), (40.0|-3.0), {(8000,10000)}, 450, 90, 5 };
                fuel { 4200, l };
                };
                };
                """.formatted(year));

        final US120ValidationResult validation = US120FlightPlanValidator.analyze(file);
        assertTrue(validation.isValid());

        final CreateFlightPlanDTO dto = US081FlightPlanMapper.map(validation.getParseTree());

        assertEquals("TP100", dto.getRouteId());
        assertEquals("TP100", dto.getFlightId());
        assertEquals("CHARTER", dto.getFlightType());
        assertEquals("CS-TST", dto.getAircraftRegistration());
        assertEquals("PILOT01", dto.getPilotId());
        assertEquals(1, dto.getLegs().size());

        final CreateFlightPlanLegDTO leg = dto.getLegs().get(0);
        assertEquals("OPO", leg.getDepartureAirportIata());
        assertEquals(Integer.valueOf(15), leg.getDepartureDay());
        assertEquals(Integer.valueOf(6), leg.getDepartureMonth());
        assertEquals(Integer.valueOf(year), leg.getDepartureYear());
        assertEquals(Integer.valueOf(9), leg.getDepartureHour());
        assertEquals(Integer.valueOf(30), leg.getDepartureMinute());
        assertEquals(Integer.valueOf(15), leg.getDepartureSecond());
        assertEquals("MAD", leg.getArrivalAirportIata());
        assertEquals(Integer.valueOf(11), leg.getArrivalHour());
        assertEquals(Integer.valueOf(45), leg.getArrivalMinute());
        assertEquals(Integer.valueOf(0), leg.getArrivalSecond());
        assertEquals(Double.valueOf(4200.0), leg.getFuelQuantity());
        assertEquals("L", leg.getFuelUnit());
    }
}
