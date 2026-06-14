package pt.isep.sem4pi.us120;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class US120SemanticValidationTest {

    @Test
    public void ensureValidExamplePassesListenerAndVisitor() throws Exception {
        final int year = LocalDate.now().getYear();
        final Path file = Files.createTempFile("valid-plan", ".dsldig5");
        Files.writeString(file, """
                flight TP100 regular {
                aircraft CS-TST;
                pilot PILOT01;
                route TP1001 { (41.24|-8.67) (40.47|-3.56) };
                leg {
                departure { OPO, 20/06/%d, 14:30 };
                arrival { MAD, 15:20 };
                segment { (41.24|-8.67), (40.47|-3.56), {(30000,35000)}, 10, 45, 850 };
                fuel { 4200, kg };
                };
                };
                """.formatted(year));

        final US120ValidationResult result = US120FlightPlanValidator.analyze(file);
        assertTrue(result.getAllErrors().toString(), result.isValid());
    }

    @Test
    public void ensureRepeatedAirportInSameLegFails() throws Exception {
        final Path file = Files.createTempFile("repeat-airport", ".dsldig5");
        Files.writeString(file, """
                flight TP100 regular {
                aircraft CS-TST;
                pilot PILOT01;
                route TP1001 { (41.24|-8.67) (40.47|-3.56) };
                leg {
                departure { OPO, 20/06/2026, 14:30 };
                arrival { OPO, 15:20 };
                segment { (41.24|-8.67), (40.47|-3.56), {(30000,35000)}, 10, 45, 850 };
                fuel { 4200, kg };
                };
                };
                """);

        final US120ValidationResult result = US120FlightPlanValidator.analyze(file);
        assertFalse(result.isValid());
        assertTrue(result.getSemanticErrors().stream()
                .anyMatch(error -> error.contains("departure and arrival airports must be different")));
    }

    @Test
    public void ensureSegmentWithSameCoordinatesFails() throws Exception {
        final Path file = Files.createTempFile("same-coords", ".dsldig5");
        Files.writeString(file, """
                flight TP100 regular {
                aircraft CS-TST;
                pilot PILOT01;
                route TP1001 { (41.24|-8.67) (40.47|-3.56) };
                leg {
                departure { OPO, 20/06/2026, 14:30 };
                arrival { MAD, 15:20 };
                segment { (41.24|-8.67), (41.24|-8.67), {(30000,35000)}, 10, 45, 850 };
                fuel { 4200, kg };
                };
                };
                """);

        final US120ValidationResult result = US120FlightPlanValidator.analyze(file);
        assertFalse(result.isValid());
        assertTrue(result.getSemanticErrors().stream()
                .anyMatch(error -> error.contains("start and end coordinates must be different")));
    }

    @Test
    public void ensureInvalidTimeReturnsSemanticErrorInsteadOfException() throws Exception {
        final Path file = Files.createTempFile("invalid-time", ".dsldig5");
        Files.writeString(file, """
                flight TP100 regular {
                aircraft CS-TST;
                pilot PILOT01;
                route TP1001 { (41.24|-8.67) (40.47|-3.56) };
                leg {
                departure { OPO, 20/06/2026, 25:90 };
                arrival { MAD, 15:20 };
                segment { (41.24|-8.67), (40.47|-3.56), {(30000,35000)}, 10, 45, 850 };
                fuel { 4200, kg };
                };
                };
                """);

        final US120ValidationResult result = US120FlightPlanValidator.analyze(file);
        assertFalse(result.isValid());
        assertTrue(result.getSemanticErrors().stream()
                .anyMatch(error -> error.contains("Time values out of bounds") || error.contains("Invalid time")));
    }

    @Test
    public void ensureFuelInLitersIsAcceptedByVisitor() throws Exception {
        final Path file = Files.createTempFile("fuel-liters", ".dsldig5");
        Files.writeString(file, """
                flight TP100 regular {
                aircraft CS-TST;
                pilot PILOT01;
                route TP1001 { (41.24|-8.67) (40.47|-3.56) };
                leg {
                departure { OPO, 20/06/2026, 14:30 };
                arrival { MAD, 15:20 };
                segment { (41.24|-8.67), (40.47|-3.56), {(30000,35000)}, 10, 45, 850 };
                fuel { 4200, l };
                };
                };
                """);

        final US120ValidationResult result = US120FlightPlanValidator.analyze(file);
        assertTrue(result.isValid());
    }
}
